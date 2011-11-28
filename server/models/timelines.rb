class Timelines

  class << self

    def all
      @timelines ||= []
      @timelines
    end

    def find id
      @timelines ||= []
      @timelines.find { |timeline| timeline.id == id }
    end

    def replace timeline
      remove timeline
      add timeline
    end

    def remove timeline
      @timelines ||= []
      @timelines.delete find(timeline)
    end

    def add timeline
      @timelines ||= []
      @timelines << timeline
    end

    def ids
      all.map &:id
    end

    def to_json
      all.to_json
    end

  end

end