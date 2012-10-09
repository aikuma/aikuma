require 'json'

class Recording
  
  attr_reader :name, :creator, :original_path
  
  def initialize name, creator, original_path
    @name, @creator, @original_path = name, creator, original_path
  end
  
  def self.load_from path, uuid
    base_path = File.join path, 'recordings'
    
    properties = {}
    File.open File.join(base_path, "#{uuid}.json") do |file|
      properties = hash_from file.read
    end
    new properties['recording_name'],
        properties['creatorUUID'],
        properties['originalUUID']
  end
  
  def self.hash_from json
    JSON.parse json
  end
  
end