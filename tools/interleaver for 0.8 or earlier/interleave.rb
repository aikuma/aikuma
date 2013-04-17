# Fail early.
#
path_with_respeakings = ARGV[0] || puts("Usage: ruby interleave.rb <relative-path-with-respeakings>\nExample:\n  ruby interleave.rb ./recordings/") || exit!

# Require necessary code.
#
require File.expand_path('../wav-file-0.0.3/lib/wav-file', __FILE__)
require File.expand_path('../interleaver', __FILE__)

interleaver = Interleaver.new

puts
i = 0
Dir[File.join(File.expand_path(path_with_respeakings, Dir.pwd), '*.map')].each do |path_with_uuid|
  puts "Processing respeaking ##{i+=1}:"
  
  path, uuid_with_extension = path_with_uuid.split(/\/([^\/]+)$/)
  uuid, _ = uuid_with_extension.split(/\./)
  interleaver.process path, uuid
end