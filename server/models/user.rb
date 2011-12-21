class User

  attr_reader :id, :name

  def initialize id, name
    @id, @name = id, name
  end

  # Save the picture for this user.
  #
  def save_picture file
    prepare_directory
    File.open picture_path, 'wb' do |target|
      target.write file.read
    end
  end
  def picture_path
    directory + "/picture.png"
  end

  # Prepare this user's directory.
  #
  def prepare_directory
    FileUtils.mkdir_p directory
  end

  # The directory in which thing that
  # belong to the user are saved.
  #
  def directory
    File.expand_path "#{BOLD_DIR}/public/user/#{id}", __FILE__
  end

  # Returns a JSON form of this user.
  #
  def to_json *a
    { id: @id, name: @name }.to_json *a
  end

  def <=> other
    self.id <=> other.id
  end

end