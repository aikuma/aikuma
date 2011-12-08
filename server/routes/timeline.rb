get '/timeline/:id.json' do
  timeline = Timelines.find params[:id]
  (timeline || {}).to_json
end

# Does the timeline exist?
#
get '/timeline/:id' do
  timeline = Timelines.find params[:id]
  status 404 unless timeline
end