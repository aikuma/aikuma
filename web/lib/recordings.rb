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
  
  def for user
    recordings = map_uuids do |uuid|
      Recording.load_from path, uuid
    end
    recordings.select { |recording| user.uuid == recording.user.uuid }
  end
  
end