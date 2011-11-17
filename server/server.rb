require 'json'

require File.expand_path '../models/user', __FILE__
require File.expand_path '../models/users', __FILE__

require 'sinatra'

# Returns the metadata of the user.
# Or an empty json hash if it cannot be found.
#
get '/user/:id.json' do
  user = Users.find params[:id]
  user.to_json
end

# Create a new user if it doesn't exist yet.
#
post '/users' do
  puts params
end

# Create a new user if it doesn't exist yet.
#
post '/user/:id/picture' do
  puts params
end

post '/timelines' do
  puts params
end