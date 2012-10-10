class Users
  
  attr_reader :path
  
  def initialize path
    @path = path
  end
  
  def map_uuids
    Dir["#{path}/users/*/metadata.json"].inject([]) do |result, file|
      file.gsub! %r{/metadata.json}, ''
      uuid = File.basename file
      result << yield(uuid)
    end
  end
  
end