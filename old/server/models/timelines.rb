class Timelines

  class << self

    def timelines
      @timelines ||= []
    end

    def all
      timelines
    end

    def find id
      timelines.find { |timeline| timeline.id == id }
    end

    def replace timeline
      remove timeline
      add timeline
    end

    def remove timeline
      timelines.delete find(timeline)
    end

    def add timeline
      timelines << timeline
    end

    def ids
      all.map &:id
    end

    def clear
      timelines.clear
    end

    def to_json
      all.to_json
    end

  end

end