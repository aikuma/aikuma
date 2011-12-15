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

  context 'segments' do
    context 'with timelines (and users)' do
      before(:each) do
        Users.add User.new('some_user_id', 'Some Name')
        Timelines.add Timeline.new('some_timeline_id',
                                   'some_prefix_',
                                   'some/date',
                                   'Somewhere',
                                   'some_user_id')
      end
      context 'without segments' do
        describe 'GET /timeline/:timeline_id/segments' do
          it 'returns an empty json array' do
            get '/timeline/some_timeline_id/segments'

            last_response.should be_ok
            last_response.body.should == '[]'
          end
        end
        describe 'GET /timeline/:timeline_id/segments/ids' do
          it 'returns an empty json array' do
            get '/timeline/some_timeline_id/segments/ids'

            last_response.should be_ok
            last_response.body.should == '[]'
          end
        end
        describe 'POST /timeline/:timeline_id/segments' do
          describe 'without any segments' do
            it 'adds a timeline to the user' do
              post '/timeline/some_timeline_id/segments', id: 'some_segment_id'

              last_response.should be_ok
              Timelines.find('some_timeline_id').segments.size.should == 1
            end
          end
        end
      end
      context 'with segments' do
        before(:each) do
          Timelines.clear # TODO Why is this needed?
          
          timeline = Timeline.new('some_timeline_id',
                                  'prefix_',
                                  'some/date',
                                  'Somewhere',
                                  'some_id')
          segment = Segment.new 'some_segment_id'
          timeline.segments.add segment
          
          Timelines.add timeline
        end
        
        describe 'GET /timeline/:id/segments' do
          it 'returns an empty json hash' do
            get '/timeline/some_timeline_id/segments'

            last_response.should be_ok
            last_response.body.should == '[{"id":"some_segment_id"}]'
          end
        end
        describe 'GET /timeline/:id/segments/ids' do
          it 'returns an empty json array' do
            get '/timeline/some_timeline_id/segments/ids'

            last_response.should be_ok
            last_response.body.should == '["some_segment_id"]'
          end
        end
        describe 'POST /timeline/:timeline_id/segments' do
          it 'adds a segment' do
            post '/timeline/some_timeline_id/segments', id: 'some_other_segment_id'

            last_response.should be_ok
            segments = Timelines.find('some_timeline_id').segments
            segments.size.should == 2
            segments.first.id.should == 'some_segment_id'
            segments[1].id.should == 'some_other_segment_id'
          end
          it 'does not add a segment twice' do
            post '/timeline/some_timeline_id/segments', id: 'some_other_segment_id'
            post '/timeline/some_timeline_id/segments', id: 'some_other_segment_id'

            last_response.should be_ok
            segments = Timelines.find('some_timeline_id').segments
            segments.size.should == 2
            segments.first.id.should == 'some_segment_id'
            segments[1].id.should == 'some_other_segment_id'
          end
          
        end
      end
    end
  end

  context 'segments' do
    context 'without timelines' do
      describe 'POST /timeline/:timeline_id/segments' do
        it 'does not add a segment' do
          Timelines.find('some_timeline_id').segments.clear
          
          post '/timeline/wrong_timeline_id/segments', id: 'other_id',
                                                       prefix: 'prefix_',
                                                       date: 'some/date',
                                                       location: 'Somewhere',
                                                       user_id: 'some_id'
          
          last_response.should be_ok
          Timelines.find('some_timeline_id').segments.should be_empty
        end
      end
    end
  end

end