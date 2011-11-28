get '/timeline/:id.json' do
  timeline = Timelines.find params[:id]
  (timeline || {}).to_json
end