package au.edu.melbuni.boldapp;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

public abstract class SpeechController implements SpeechTriggers {

	protected int samplingRate = 1000; // TODO Get as much as possible.
	protected int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	protected int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//	protected int bufferSize = AudioRecord.getMinBufferSize(samplingRate,
//			channelConfig, audioFormat);

	protected short[] buffer = new short[samplingRate];
	protected AudioRecord listener;

	// protected boolean listening = false;

	public SpeechController() {
		setUpListener();
	}

	protected void setUpListener() {
		waitForAudioRecord();
	}

	// protected void waitForAudioRecord() {
	// int i = 0;
	// while (listener.getState() != AudioRecord.STATE_INITIALIZED) {
	// if (i++ > 5) {
	// throw new RuntimeException(
	// this.getClass().toString() + ": listener is not initialized.");
	// }
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }

	public void waitForAudioRecord() {
		int index = 0;
		do {
			listener = getListener(index, AudioFormat.ENCODING_PCM_16BIT,
					AudioFormat.CHANNEL_CONFIGURATION_MONO);
			index += 1;
		} while (listener != null
				&& (listener.getState() != AudioRecord.STATE_INITIALIZED));
	}

	private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };

	public static AudioRecord getListener(int index, int audioFormat, int channelConfig) {
		if (index >= sampleRates.length) {
			return null;
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
		int sampleRate = sampleRates[index];
		int framePeriod = sampleRate * 120 / 1000; // 120 is the timer interval.
		int bufferSize = framePeriod * 2 * sampleSize * numberOfChannels / 8;

		return new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRates[index], AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	public void listen(String sourceFilename, String targetFilename, OnCompletionListener completionListener) {
		listener.startRecording();
		// listening = true;
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
		// listening = false;
	}

	protected void read() {
		while (listener.read(buffer, 0, buffer.length) > 0) {
			// Hand in a copy of the buffer.
			//
			onBufferFull(Arrays.copyOf(buffer, buffer.length));
		}
	}

	protected abstract void onBufferFull(short[] buffer);

	public abstract void silenceTriggered(short[] buffer, boolean justChanged);

	public abstract void speechTriggered(short[] buffer, boolean justChanged);
}
