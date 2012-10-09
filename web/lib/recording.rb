require 'json'

require_relative 'creator'

class Recording
  
  attr_reader :name, :creator, :path
  
  def initialize name, creator, path
    @name, @creator, @path = name, creator, path
  end
  
  def self.load_from path, uuid
    base_path = File.join path, 'recordings'
    
    properties = {}
    File.open File.join(base_path, "#{uuid}.json") do |file|
      properties = hash_from file.read
    end
    new properties['recording_name'],
        Creator.load_from(path, properties['creatorUUID']),
        File.join(base_path, "#{properties['uuid']}.wav")
  end
  
  def self.hash_from json
    JSON.parse json
  end
  
end