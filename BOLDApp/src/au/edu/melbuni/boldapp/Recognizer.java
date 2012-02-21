package au.edu.melbuni.boldapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

/*
 * A Recognizer tries to recognize silence or talk from a user and starts and stops
 * his player and recorder appropriately.
 * 
 * Note: Used for respeaking.
 */
public class Recognizer {

	Player player;
	Recorder recorder;

	protected int samplingRate = 8000;
	protected int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	protected int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	protected int bufferSize = AudioRecord.getMinBufferSize(samplingRate,
			channelConfig, audioFormat);
	protected short[] buffer = new short[samplingRate];
	AudioRecord listener = new AudioRecord(MediaRecorder.AudioSource.MIC,
			samplingRate, channelConfig, audioFormat, bufferSize);

	int maxAmplitude;
	int silenceThreshold;
	int speechThreshold;

	boolean listening = false;

	public Recognizer() {
		player = Bundler.getPlayer();
		recorder = Bundler.getRecorder();
		setUpListener();
	}

	protected void setUpListener() {
		maxAmplitude = 32768; // MediaRecorder.getAudioSourceMax(); // TODO Make
								// dynamic depending on phone.
		LogWriter.log("Max amplitude is: " + maxAmplitude);
		speechThreshold = maxAmplitude / 3; // Speech is more than 1/m of max
											// amplitude.
		silenceThreshold = maxAmplitude / 10; // Silence is less than 1/n of max
												// amplitude.

		waitForAudioRecord();
	}

	protected void waitForAudioRecord() {
		int i = 0;
		while (listener.getState() != AudioRecord.STATE_INITIALIZED) {
			if (i++ > 5) {
				throw new RuntimeException(
						"Recognizer: AudioRecord is not initialized.");
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// To be called from the thread.
	//
	protected void read() {
		while (listener.read(buffer, 0, buffer.length) > 0) {
			onBufferFull(buffer);
		}
	}

	/*
	 * Starts listening and starts/stops the player/recorder appropriately.
	 */
	public void listen(String fileName, OnCompletionListener completionListener) {
		listener.startRecording();
		listening = true;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				read();
			}
		});
		t.start();
		player.startPlaying(fileName, completionListener);
	}

	/*
	 * Switches the mode to play mode.
	 */
	protected void switchToPlay() {
		recorder.stopRecording();
		player.resume();
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();
		// recorder.startRecording("test"); // FIXME Make this dynamic!
	}

	/*
	 * Stops listening.
	 */
	public void stop() {
		player.stopPlaying();
		listener.stop();
		listening = false;
	}

	/*
	 * Whether we have silence.
	 */
	public boolean isSilence(int reading) {
		return reading < silenceThreshold;
	}

	/*
	 * Whether we have speech.
	 */
	public boolean isSpeech(int reading) {
		return reading > speechThreshold;
	}

	protected int getMaxAmplitude(short[] buffer) {
		short maxValue = 0;

		// Check every 5th sample.
		//
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] > maxValue) {
				maxValue = buffer[i];
			}
		}

		return maxValue;
	}

	public void onBufferFull(short[] buffer) {
		int reading = getMaxAmplitude(buffer);

		// Check if we need to stop.
		//
		if (isSilence(reading)) {
			LogWriter.log("Silence.");
			switchToPlay();
		} else {
			if (isSpeech(reading)) {
				LogWriter.log("Speech.");
				switchToRecord();
			} else {
				// else just continue doing what it does.
				//
				LogWriter.log("---");
			}
		}
	}

}
