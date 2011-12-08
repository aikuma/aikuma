class Users

  def self.all
    @users ||= []
    @users
  end

  def self.find id
    @users ||= []
    @users.find { |user| user.id == id }
  end

  def self.replace user
    remove user
    add user
  end

  def self.remove user
    @users ||= []
    @users.delete find(user)
  end

  def self.add user
    @users ||= []
    @users << user
  end

  def self.ids
    all.map &:id
  end

  def self.clear
    @users.clear
  end

  def self.to_json
    all.to_json
  end

end