# Returns the metadata of the user.
# Or an empty json hash if it cannot be found.
#
get '/user/:id.json' do
  user = Users.find params[:id]
  (user || {}).to_json
end

# Does the user exist?
#
get '/user/:id' do
  user = Users.find params[:id]
  status 404 unless user
end

# Saves the given png.
#
post '/user/:id/picture' do
  user = Users.find params[:id]
  if user
    user.save_picture params[:file][:tempfile]
  else
    status 404
  end
end