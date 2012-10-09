require 'sinatra'
require 'haml'

require_relative 'lib/recording'
require_relative 'lib/recordings'
require_relative 'lib/user'
require_relative 'lib/users'

# Shows the recordings grouped by users.
#
get %r{(?<path>.+)\/users\/?$} do
  @path = params[:path]
  
  @users = Users.new(@path).map_uuids do |uuid|
    user = User.load_from @path, uuid
    user.load_recordings @path
    user
  end
  @users.sort_by! &:name
  
  haml :users
end

# Shows all the recordings.
#
get %r{(?<path>.+)\/recordings\/?$} do
  @path = params[:path]
  
  @recordings = Recordings.new(@path).map_uuids do |uuid|
    Recording.load_from @path, uuid
  end
  
  haml :recordings
end

# Returns a file if requested.
#
get %r{(?<path>.+)\/recordings\/(?<uuid>[0-9a-f\-]+)\.\w+$} do
  recording = Recording.load_from params[:path], params[:uuid]
  
  send_file recording.path
end