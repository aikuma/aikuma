class Creator
  
  attr_reader :name
  
  def initialize name
    @name = name
  end
  
  def self.load_from path, uuid
    base_path = File.join path, 'users'
    
    properties = {}
    File.open File.join(base_path, uuid, "metadata.json") do |file|
      properties = hash_from file.read
    end
    
    new properties['name']
  end
  
  def self.hash_from json
    JSON.parse json
  end
  
end