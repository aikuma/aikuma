# Returns the metadata of the user.
# Or an empty json hash if it cannot be found.
#
get '/user/:id.json' do
  user = Users.find params[:id]
  (user || {}).to_json
end

# Saves the given png.
#
post '/user/:id/picture' do
  user = Users.find params[:id]
  user.save_picture params[:file][:tempfile] if user
end