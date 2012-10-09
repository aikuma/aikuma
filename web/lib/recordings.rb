class Recordings
  
  attr_reader :path
  
  def initialize path
    @path = path
  end
  
  def map_uuids
    Dir["#{path}/recordings/*.json"].inject([]) do |result, file|
      uuid = File.basename(file).gsub(/\.\w+$/, '')
      result << yield(uuid)
    end
  end
  
end