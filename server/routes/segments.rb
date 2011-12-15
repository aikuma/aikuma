get '/timeline/:timeline_id/segments' do
  timeline = Timelines.find params[:timeline_id]
  (timeline && timeline.segments || []).to_json
end

get '/timeline/:timeline_id/segments/ids' do
  timeline = Timelines.find params[:timeline_id]
  (timeline && timeline.segments.ids || []).to_json
end

#
#
post '/timeline/:timeline_id/segments' do
  segment  = Segment.new params[:id]
  timeline = Timelines.find params[:timeline_id]
  
  if segment && timeline
    if timeline.segments.empty?
      timeline.segments.add segment
    else
      segments = timeline.segments
      index = segments.index segment
      index ? segments.insert(index, segment) : segments.add(segment)
    end
    status 200
  end
end