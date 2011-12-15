class Users

  class << self

    def users
      @users ||= []
    end

    def all
      users
    end

    def find id
      users.find { |user| user.id == id }
    end

    def replace user
      remove user
      add user
    end

    def remove user
      users.delete find(user)
    end

    def add user
      users << user
    end

    def ids
      all.map &:id
    end

    def clear
      users.clear
    end

    def to_json
      all.to_json
    end

  end

end