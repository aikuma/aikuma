class Timeline

  attr_reader :id, :prefix, :date, :location, :user_id,
              :segments

  def initialize id, prefix, date, location, user_id
    @id, @prefix, @date, @location, @user_id = id, prefix, date, location, user_id
    @segments = Segments.new
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
    File.expand_path "../../public/timeline/#{id}", __FILE__
  end

  # Returns a JSON form of this user.
  #
  def to_json *a
    {
      id:       @id,
      prefix:   @prefix,
      date:     @date,
      location: @location,
      user_id:  @user_id
    }.to_json *a
  end

  def <=> other
    self.id <=> other.id
  end

end