class Interleaver
  
  def process path, uuid
    respeaking_wav_file      = File.expand_path File.join(path, "#{uuid}.wav")
    respeaking_metadata_file = File.expand_path File.join(path, "#{uuid}.json")
    respeaking_mapping_file  = File.expand_path File.join(path, "#{uuid}.map")

    require 'json'

    # Extract the metadata.
    #
    respeaking_metadata = JSON.load File.open respeaking_metadata_file
    original_uuid = respeaking_metadata['original_uuid']
    raise "You need to give it a respeaking file!" unless original_uuid
    original_wav_file = File.expand_path File.join(path, "#{original_uuid}.wav")
    
    puts original_wav_file
    puts '+'
    puts respeaking_wav_file

    # Prepare.
    #
    format = nil
    data_chunk = nil
    result_data = []

    # Reconstitute the files.
    #
    File.open(original_wav_file, 'r') do |original|
      File.open(respeaking_wav_file, 'r') do |respeaking|
    
        format = WavFile.readFormat original
    
        data_chunk = WavFile.readDataChunk(original)
        original_data   = data_chunk.data.unpack('s*')
        respeaking_data = WavFile.readDataChunk(respeaking).data.unpack('s*')
    
        last_original_offset = 0
        last_respeaking_offset = 0
        File.open(respeaking_mapping_file, 'r') do |mapping|
          mapping.each_line do |line|
            original_offset, respeaking_offset = line.chomp.split(',').map &:to_i
        
            next unless original_offset
            original_offset += 44
            bytes_from_original = original_offset - last_original_offset
            next if bytes_from_original < 0
            result_data += original_data.shift(bytes_from_original)
            last_original_offset = original_offset
        
            next unless respeaking_offset
            respeaking_offset += 44
            bytes_from_respeaking = respeaking_offset - last_respeaking_offset
            next if bytes_from_respeaking < 0
            result_data += respeaking_data.shift(bytes_from_respeaking)
            last_respeaking_offset = respeaking_offset
          end
        end
      end
    end

    # Rechunk the data and save.
    #
    data_chunk.data = result_data.pack('s*')
    File.open File.expand_path("#{uuid}.interleaved.wav"), 'w' do |result_wav|
      WavFile.write result_wav, format, [data_chunk]
      puts '='
      puts result_wav.path
      puts
    end
  rescue
    puts
    puts "Processing respeaking\n  #{respeaking_wav_file}\nfailed. Sorry!"
    puts
  end

end