get '/users' do
  Users.to_json
end

get '/users/ids' do
  Users.ids.to_json
end

# Create a new user if it doesn't exist yet.
#
post '/users' do
  user = User.new params[:id], params[:name]
  Users.replace user if user
  status 200
end