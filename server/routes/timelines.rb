# All.
#
get '/timelines' do
  Timelines.to_json
end

get '/timelines/ids' do
  Timelines.ids.to_json
end

# User specific.
#
get '/user/:id/timelines' do
  user = Users.find params[:id]
  (user && user.timelines || {}).to_json
end

get '/user/:id/timelines/ids' do
  user = Users.find params[:id]
  (user && user.timelines && user.timelines.ids || {}).to_json
end

# Create a new timeline if it doesn't exist yet.
#
post '/timelines' do
  timeline = Timeline.new params[:id],
                          params[:prefix],
                          params[:date],
                          params[:location] || "",
                          params[:user_id]
  user = Users.find params[:user_id]
  Timelines.replace timeline if timeline && user
end