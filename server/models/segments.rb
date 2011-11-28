class Segments

  class << self

    def all
      @segments ||= []
      @segments
    end

    def find id
      @segments ||= []
      @segments.find { |segment| segment.id == id }
    end

    def replace segment
      remove segment
      add segment
    end

    def remove segment
      @segments ||= []
      @segments.delete find(segment)
    end

    def add segment
      @segments ||= []
      @segments << segment
    end

    def ids
      all.map &:id
    end

    def to_json
      all.to_json
    end

  end

end