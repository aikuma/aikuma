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

  context 'timelines' do
    context 'with users' do
      before(:each) do
        Users.add User.new('some_id', 'Some Name')
      end
      context 'without timelines' do
        describe 'GET /timelines' do
          it 'returns an empty json array' do
            get '/timelines'
            last_response.should be_ok
            last_response.body.should == '[]'
          end
        end
        describe 'GET /timelines/ids' do
          it 'returns an empty json array' do
            get '/timelines/ids'
            last_response.should be_ok
            last_response.body.should == '[]'
          end
        end
        describe 'POST /timelines' do
          it 'adds a timeline to the user' do
            post '/timelines', id: 'other_id',
                               prefix: 'prefix_',
                               date: 'some/date',
                               location: 'Somewhere',
                               user_id: 'some_id'
            last_response.should be_ok

            Timelines.all.size.should == 1

            first = Timelines.all.first
            first.id.should == 'other_id'
            first.prefix.should == 'prefix_'
            first.date.should == 'some/date'
            first.location.should == 'Somewhere'
            first.user_id.should == 'some_id'
          end
          it 'does not add a timeline to the user' do
            post '/timelines', id: 'other_id',
                               prefix: 'prefix_',
                               date: 'some/date',
                               location: 'Somewhere',
                               user_id: 'wrong_id'
            last_response.should be_ok

            Timelines.all.should be_empty
          end
        end
      end
      context 'with timelines' do
        before(:each) do
          Timelines.add Timeline.new('other_id',
                                     'prefix_',
                                     'some/date',
                                     'Somewhere',
                                     'some_id')
        end
        describe 'GET /timelines' do
          it 'returns an empty json array' do
            get '/timelines'

            last_response.should be_ok
            last_response.body.should == '[{"id":"other_id","prefix":"prefix_","date":"some/date","location":"Somewhere","user_id":"some_id"}]'
          end
        end
        describe 'GET /timelines/ids' do
          it 'returns an empty json array' do
            get '/timelines/ids'

            last_response.should be_ok
            last_response.body.should == '["other_id"]'
          end
        end
        describe 'POST /timelines' do
          it 'adds a timeline' do
            post '/timelines', id: 'other_id',
                               prefix: 'prefix_',
                               date: 'some/date',
                               location: 'Somewhere',
                               user_id: 'some_id'
            last_response.should be_ok

            Timelines.all.size.should == 2

            first = Timelines.all.first
            first.id.should == 'other_id'
            first.prefix.should == 'prefix_'
            first.date.should == 'some/date'
            first.location.should == 'Somewhere'
            first.user_id.should == 'some_id'
          end
        end
      end
    end
  end

  context 'timelines' do
    context 'without users' do
      describe 'POST /timelines' do
        it 'does not add a timeline' do
          post '/timelines', id: 'other_id',
                             prefix: 'prefix_',
                             date: 'some/date',
                             location: 'Somewhere',
                             user_id: 'some_id'
          last_response.should be_ok

          Timelines.all.should be_empty
        end
      end
    end
  end

end