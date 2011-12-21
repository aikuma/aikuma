# coding: utf-8
#
require_relative '../../server'
require_relative 'setup'

require 'rack/test'
require 'rspec'

# describe 'Synchronisation' do
#   include Rack::Test::Methods
#
#   def app
#     Sinatra::Application
#   end
#
#
#   describe 'GET /timeline/:timeline_id/segments' do
#     it 'returns an empty json array' do
#       get '/timeline/some_timeline_id/segments'
#
#       last_response.should be_ok
#       last_response.body.should == '[]'
#     end
#   end
# end
