# Returns the metadata of the segment.
# Or an empty json hash if it cannot be found.
#
get '/segment/:id.json' do
  segment = Segments.find params[:id]
  (segment || {}).to_json
end

# Does the segment exist?
#
get '/segment/:id' do
  segment = Segments.find params[:id]
  status 404 unless segment
end

# Saves the given gp3.
#
post '/segment/:id/soundfile' do
  segment = Segments.find params[:id]
  segment.save_soundfile params[:file][:tempfile] if segment
end