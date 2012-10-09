require 'json'

require_relative 'user'

class Recording
  
  attr_reader :uuid, :name, :user, :path
  
  def initialize uuid, name, user, path
    @uuid, @name, @user, @path = uuid, name, user, path
  end
  
  def self.load_from path, uuid
    base_path = File.join path, 'recordings'
    
    properties = {}
    File.open File.join(base_path, "#{uuid}.json") do |file|
      properties = hash_from file.read
    end
    new uuid,
        properties['recording_name'],
        User.load_from(path, properties['creatorUUID']),
        File.join(base_path, "#{properties['uuid']}.wav")
  end
  
  def self.hash_from json
    JSON.parse json
  end
  
end