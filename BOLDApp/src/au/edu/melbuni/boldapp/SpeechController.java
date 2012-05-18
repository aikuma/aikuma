package au.edu.melbuni.boldapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

public abstract class SpeechController {

	int maxAmplitude;
	protected int silenceThreshold;
	protected int speechThreshold;
	
	protected int samplingRate = 8000;
	protected int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	protected int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	protected int bufferSize = AudioRecord.getMinBufferSize(samplingRate,
				channelConfig, audioFormat);
	
	protected short[] buffer = new short[samplingRate];
	protected AudioRecord listener = new AudioRecord(MediaRecorder.AudioSource.MIC,
				samplingRate, channelConfig, audioFormat, bufferSize);
	
	protected boolean listening = false;
	
	public SpeechController() {
		setUpListener();
	}
	
	protected void setUpListener() {
		maxAmplitude = 32768; // MediaRecorder.getAudioSourceMax(); // TODO Make
								// dynamic depending on phone.
		LogWriter.log("Max amplitude is: " + maxAmplitude);
		speechThreshold = maxAmplitude / 6; // Speech is more than 1/m of max
											// amplitude.
		silenceThreshold = maxAmplitude / 6; // Silence is less than 1/n of max
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
	}

	public void stop() {
		listener.stop();
		listening = false;
	}

	protected void read() {
		while (listener.read(buffer, 0, buffer.length) > 0) {
			onBufferFull(buffer);
		}
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
	
	protected boolean isSpeech(int reading) {
		return reading > speechThreshold;
	}
	
	protected boolean isSilence(int reading) {
		return reading < silenceThreshold;
	}
	
	protected abstract void onBufferFull(short[] buffer);
}
