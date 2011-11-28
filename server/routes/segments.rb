get '/timeline/:id/segments' do
  timeline = Timelines.find params[:id]
  (timeline && timeline.segments || {}).to_json
end

get '/timeline/:id/segments/ids' do
  timeline = Timelines.find params[:id]
  (timeline && timeline.segments && timeline.segments.map(&:id) || {}).to_json
end

# Create a new segment if it doesn't exist yet.
#
post '/segments' do
  segment = Segment.new params[:id]
  Segments.replace segment if segment
end

post '/timeline/:timeline_id/segments' do
  segment  = Segment.new params[:id]
  timeline = Timelines.find params[:timeline_id]
  if segment && timeline
    if timeline.segments.empty?
      timeline.segments << segment
    else
      i = timeline.segments.index segment
      timeline.segments.insert i, segment
    end
  end
end