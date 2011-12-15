class Segment

  attr_reader :id

  def initialize id
    @id = id
    Segments << self
  end

  # Save the picture for this user.
  #
  def save_soundfile file
    prepare_directory
    File.open soundfile_path, 'wb' do |target|
      target.write file.read
    end
  end
  def soundfile_path
    directory + "/soundfile.gp3"
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
    File.expand_path "../../public/segment/#{id}", __FILE__
  end

  # Returns a JSON form of this user.
  #
  def to_json *a
    { id: @id }.to_json *a
  end

  def <=> other
    self.id <=> other.id
  end

end