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

  context 'timeline' do
    context 'without timelines' do
      describe 'GET /timeline/:id.json' do
        it 'returns an empty json hash' do
          get '/timeline/some_id.json'

          last_response.should be_ok
          last_response.body.should == '{}'
        end
      end
      describe 'GET /timeline/:id' do
        it 'returns a 404' do
          get '/timeline/some_id'

          last_response.status.should == 404
        end
      end
    end
    context 'with timelines' do
      let(:timeline) { Timeline.new('some_id', 'some_prefix_', 'some/date', 'Some Location', 'user_id') }
      before(:each) do
        Timelines.clear
        Timelines.add timeline
      end
      describe 'GET /timeline/:id.json' do
        it 'returns an empty json hash' do
          get '/timeline/some_id.json'

          last_response.should be_ok
          last_response.body.should == '{"id":"some_id","prefix":"some_prefix_","date":"some/date","location":"Some Location","user_id":"user_id"}'
        end
      end
      describe 'GET /timeline/:id' do
        it 'returns the timeline' do
          get '/timeline/some_id'

          last_response.should be_ok
        end
      end
    end
  end

end