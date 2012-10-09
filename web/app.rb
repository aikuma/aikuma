require 'sinatra'
require 'haml'

require_relative 'lib/recording'
require_relative 'lib/recordings'

get %r{(?<path>[\/\w]+)/recordings} do
  @path = params[:path]
  
  @recordings = Recordings.new(@path).map_uuids do |uuid|
    Recording.load_from @path, uuid
  end
  
  haml :recordings
end