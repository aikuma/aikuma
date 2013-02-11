package au.edu.unimelb.aikuma.audio;

import java.util.Arrays;
import java.util.Set;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.analyzers.SimpleAnalyzer;

/** A Recorder is used to get input from eg. a microphone and
 *  output into a file.
 * 
 *  Usage:
 *    Recorder recorder = new Recorder();
 *    recorder.prepare("/mnt/sdcard/bold/recordings/target_file.wav")
 *    recorder.listen();
 *    recorder.pause();
 *    recorder.listen();
 *    recorder.stop();
 *
 *  Note that stopping the recorder closes and finalizes the WAV file.
 */
public class Recorder implements AudioHandler {

	protected Thread t;

	/** Recording buffer.
	 *
	 *  Used to ferry samples to a PCM based file/consumer.
	 */
	protected short[] buffer = new short[1000];

	/** AudioRecord listens to the microphone */
	protected AudioRecord listener;

	/** File to write to. */
	protected PCMWriter file;

	/** Analyzer that analyzes the incoming data. */
	Analyzer analyzer;

	/** Default constructor.
	 *
	 *Note: Uses an analyzer which tells the recorder to always record.
	 */
	public Recorder() {
		this(new SimpleAnalyzer());
	}

	/** Default constructor.
	 *
	 * @param Pass in an analyzer which decides whether
	 *        the recorder should record or ignore the input.
	 *
	 * Note: Uses default recording parameters.
	 */
	public Recorder(Analyzer analyzer) {
		this.analyzer = analyzer;

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

	/** Waits for the listening device. */
	public void waitForAudioRecord() {
		listener = getListener(Constants.SAMPLE_RATE,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.CHANNEL_CONFIGURATION_MONO);
		do {
		} while (listener.getState() != AudioRecord.STATE_INITIALIZED);
	}

	/** Tries to get a listening device for the built-in/external microphone.
	 *
	 * Note: It converts the Android parameters into
	 * parameters that are useful for AudioRecord.
	 */
	protected static AudioRecord getListener(
			int sampleRate, int audioFormat, int channelConfig) {

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

		/** The period used for callbacks to onBufferFull. */
		int framePeriod = sampleRate * 120 / 1000;

		/** The buffer needed for the above period */
		int bufferSize = framePeriod * 2 * sampleSize * numberOfChannels / 8;

		return new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	/**
	 * Prepares the recorder for recording.
	 */
	public void prepare(String targetFilename) {
		file.prepare(targetFilename);
	}

	/** Start listening. */
	public void listen() {

		// If there is already a thread listening then kill it and ensure it's
		// dead before creating a new thread.
		if (t != null) {
			t.interrupt();
			while (t.isAlive()) {
			}
		}

		// Simply reads and reads...
		//
		t = new Thread(new Runnable() {
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
		do {
		} while (listener.getState() != AudioRecord.RECORDSTATE_STOPPED);
		file.close();
	}

	/** Pause listening to the microphone. */
	public void pause() {
		listener.stop();
		do {
		} while (listener.getState() != AudioRecord.RECORDSTATE_STOPPED);
	}

	/** Read from the listener's buffer and call the callback. */
	protected void read() {
		// Start listening to the audio device.
		listener.startRecording();

		// Wait until something is heard.
		while (listener.read(buffer, 0, buffer.length) > 0) {
			// Hand in a copy of the buffer.
			//
			if (Thread.interrupted()) {
				return;
			}
			onBufferFull(Arrays.copyOf(buffer, buffer.length));
		}
	}

	/** As soon as enough data has been read, this method
	 *  will be called, allowing the recorder to handle
	 *  the incoming data using an analyzer.
	 */
	protected void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		//
		analyzer.analyze(this, buffer);
	}


	//The following two methods handle silences/speech
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
		// Intentionally empty.
	}
}
