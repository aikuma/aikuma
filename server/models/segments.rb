class Segments

  def segments
    @segments ||= []
  end

  def all
    segments
  end

  def find id
    segments.find { |segment| segment.id == id }
  end

  def replace segment
    remove segment
    add segment
  end

  def remove segment
    segments.delete find(segment.id)
  end

  def add segment
    segments << segment
  end

  def ids
    segments.map &:id
  end
  
  def first
    segments.first
  end
  
  def [] index
    segments[index]
  end
  
  def index _
    segments.index _
  end

  def clear
    segments.clear
  end
  
  def empty?
    segments.empty?
  end
  
  def size
    segments.size
  end

  def to_json
    segments.to_json
  end

end