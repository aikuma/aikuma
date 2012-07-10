# coding: utf-8
#
require_relative '../server'

require 'rack/test'
require 'rspec'

describe 'Server' do
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  before(:each) do
    Storage.clear
  end

  context 'users' do
    context 'without users' do
      describe 'GET /users' do
        it 'returns an empty json array' do
          get '/users'
          last_response.should be_ok
          last_response.body.should == '[]'
        end
      end
      describe 'GET /users/ids' do
        it 'returns an empty json array' do
          get '/users/ids'
          last_response.should be_ok
          last_response.body.should == '[]'
        end
      end
      describe 'POST /users' do
        it 'adds a user' do
          post '/users', id: 'some_id', name: 'Some Name'
          last_response.should be_ok

          Users.all.size.should == 1
          Users.all.first.id.should == 'some_id'
          Users.all.first.name.should == 'Some Name'
        end
      end
    end
    context 'with users' do
      before(:each) do
        Users.add User.new('first_id', 'First Name')
      end
      describe 'GET /users' do
        it 'returns an empty json array' do
          get '/users'

          last_response.should be_ok
          last_response.body.should == '[{"id":"first_id","name":"First Name"}]'
        end
      end
      describe 'GET /users/ids' do
        it 'returns an empty json array' do
          get '/users/ids'

          last_response.should be_ok
          last_response.body.should == '["first_id"]'
        end
      end
      describe 'POST /users' do
        it 'adds a user' do
          post '/users', id: 'some_id', name: 'Some Name'
          last_response.should be_ok

          Users.all.size.should == 2
          Users.all.first.id.should == 'first_id'
          Users.all.first.name.should == 'First Name'
          Users.all[1].id.should == 'some_id'
          Users.all[1].name.should == 'Some Name'
        end
      end
    end
  end

end