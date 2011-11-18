class Users

  def self.all
    @users ||= []
    @users
  end

  def self.find id
    @users ||= []
    @users.find { |user| user.id == id }
  end

  def self.add user
    @users ||= []
    @users << user
  end

  def self.delete id
    @users.delete id
  end

end