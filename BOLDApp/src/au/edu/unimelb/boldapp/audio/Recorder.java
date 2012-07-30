package au.edu.melbuni.boldapp.audio;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import PCMWriter;

/**
 *
 */
public abstract class Recorder implements SpeechTriggers {

	protected int samplingRate;
	protected int channelConfig;
	protected int audioFormat;
  
  /** Recording buffer.
   *  
   *  Used to ferry samples to a PCM based file/consumer.
   */
	protected short[] buffer = new short[samplingRate];
  
  /** AudioRecord listens to the microphone */
	protected AudioRecord listener;
  
  /** File to write to. */
  protected PCMWriter file;
  
  /** Analyzer that analyzes the incoming data. */
  Analyzer analyzer;
  
  /** Default constructor.
   *
   * Note: Uses default recording parameters.
   */
	public Recorder() {
    this(new )
	}
  
  public Recorder(Analyzer analyzer) {
    this.analyzer = analyzer;
    
  	samplingRate = 1000;
    audioFormat = AudioFormat.ENCODING_PCM_16BIT;
  	channelConfig = AudioFormat.CHANNEL_IN_MONO;
    
		setUpListener();
    setUpFile();
  }
  
  /** Sets up the listening device. Eg. the microphone. */
	protected void setUpListener() {
		waitForAudioRecord();
	}
  
  /** Sets the file up for writing. */
  protected void setUpFile() {
		file = PCMWriter.getInstance(listener.getSampleRate(),
				listener.getChannelConfiguration(), listener.getAudioFormat());
  }
  
  /** Waits for the listening device.
   *
   * Note: This goes through all the sample
   * rates until it finds one the device supports.
   */
	public void waitForAudioRecord() {
		int index = 0;
		do {
			listener = getListener(index, AudioFormat.ENCODING_PCM_16BIT,
					AudioFormat.CHANNEL_CONFIGURATION_MONO);
			index += 1;
		} while (listener != null
				&& (listener.getState() != AudioRecord.STATE_INITIALIZED));
	}
  
  /** List of sample rates we want the device to try. */
	private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };
  
  /** Tries to get a listening device for the built-in/external microphone.
   *
   * Note: It converts the Android parameters into
   * parameters that are useful for AudioRecord.
   */
	public static AudioRecord getListener(int index, int audioFormat, int channelConfig) {
		if (index >= sampleRates.length) {
			index = sampleRates.length - 1; // Fall back.
		}

		// Sample size.
		//
		int sampleSize;
		if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
			sampleSize = 16;
		} else {
			sampleSize = 8;
		}

		// Channels.
		//
		int numberOfChannels;
		if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
			numberOfChannels = 1;
		} else {
			numberOfChannels = 2;
		}
		
		// Calculate buffer size.
		//
    
    /** Get the right sample rate. */
		int sampleRate = sampleRates[index];
    
    /** The period used for callbacks to onBufferFull. */
		int framePeriod = sampleRate * 120 / 1000;
    
    /** The buffer needed for the above period */
		int bufferSize = framePeriod * 2 * sampleSize * numberOfChannels / 8;

		return new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}
  
  /** Start listening. */
	public void listen(String targetFilename) {
    // Prepare the target file for writing.
    //
    file.prepare(targetFilename);
    
    // Start listening to the audio device.
    //
    listener.startRecording();
    
    // Simply reads and reads...
    //
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				read();
			}
		});
		t.start();
	}
  
  /** Stop listening to the microphone and close the file.
   *
   * Note: Once stopped you cannot restart the recorder.
   */
	public void stop() {
		listener.stop();
    file.close();
	}
  
  /** Pause listening to the microphone. */
	public void pause() {
		listener.stop();
	}
  
  /** Resume listening to the microphone. */
	public void resume() {
		listener.startRecording();
	}
  
  /** Read from the listener's buffer and call the callback. */
	protected void read() {
		while (listener.read(buffer, 0, buffer.length) > 0) {
			// Hand in a copy of the buffer.
			//
			onBufferFull(Arrays.copyOf(buffer, buffer.length));
		}
	}
  
  /** As soon as enough data has been read, this method
   *  will be called, allowing the recorder to handle
   *  the incoming data using an analyzer.
   */
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
    //  * silenceTriggered
    //  * audioTriggered
		//
		analyzer.analyze(this, buffer);
	}

  
  // The following two methods handle silences/speech
  // discovered in the input data.
  //
  // If you need a different behaviour, override.
  //
  
  /** By default simply writes the buffer to the file. */
	public void audioTriggered(short[] buffer, boolean justChanged) {
		file.write(buffer);
	}
  
  /** Does nothing by default if silence is triggered. */
	public void silenceTriggered(short[] buffer, boolean justChanged) {
    
	}
}
