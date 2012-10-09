class User
  
  attr_reader :uuid, :name, :recordings
  
  def initialize uuid, name
    @uuid, @name = uuid, name
  end
  
  def self.load_from path, uuid
    base_path = File.join path, 'users'
    
    properties = {}
    File.open File.join(base_path, uuid, "metadata.json") do |file|
      properties = hash_from file.read
    end
    
    new uuid, properties['name']
  end
  
  def self.hash_from json
    JSON.parse json
  end
  
  def load_recordings path
    @recordings = Recordings.new(path).for self
  end
  
end