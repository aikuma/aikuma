# Returns the metadata of the user.
# Or an empty json hash if it cannot be found.
#
get '/user/:id.json' do
  user = Users.find params[:id]
  (user || {}).to_json
end

get '/users' do
  Users.all.to_json
end

get '/users/ids' do
  Users.ids.to_json
end

# Create a new user if it doesn't exist yet.
#
post '/users' do
  user = User.new params[:id], params[:name]
  Users.replace user if user
end

# Saves the given png.
#
post '/user/:id/picture' do
  user = Users.find params[:id]
  user.save_picture params[:file][:tempfile] if user
end

# Static.
#
# # Return the picture.
# #
# get '/user/:id/picture.png' do
#   user = User.find params[:id]
#   if user
#
#   end
# end
