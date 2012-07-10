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

  context 'user' do
    context 'without users' do
      describe 'GET /user/:id.json' do
        it 'returns an empty json hash' do
          get '/user/some_id.json'

          last_response.should be_ok
          last_response.body.should == '{}'
        end
      end
      describe 'GET /user/:id' do
        it 'returns a 404' do
          get '/user/some_id'

          last_response.status.should == 404
        end
      end
      describe 'POST /user/:id/picture' do
        it 'adds a user' do
          post '/user/some_id/picture', file: { tempfile: 'filedata' }

          last_response.status.should == 404
        end
      end
    end
    context 'with users' do
      let(:user) { User.new('first_id', 'First Name') }
      before(:each) do
        Users.clear
        Users.add user
      end
      describe 'GET /user/:id.json' do
        it 'returns an empty json hash' do
          get '/user/first_id.json'

          last_response.should be_ok
          last_response.body.should == '{"id":"first_id","name":"First Name"}'
        end
      end
      describe 'GET /user/:id' do
        it 'returns the user' do
          get '/user/first_id'

          last_response.should be_ok
        end
      end
      describe 'POST /user/:id/picture' do
        it 'adds a picture to the user' do
          user.stub! :save_picture

          post '/user/first_id/picture', file: { tempfile: 'filedata' }

          last_response.should be_ok
        end
        it 'adds the right picture data to the user' do
          user.should_receive(:save_picture).once.with 'filedata'

          post '/user/first_id/picture', file: { tempfile: 'filedata' }
        end
      end
    end
  end

end