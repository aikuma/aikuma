package org.lp20.aikuma.audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.util.Arrays;
import java.util.Set;

/**
 * A Microphone is used to get input from the physical microphone
 * and yields buffers in a callback.
 *
 *  Usage:
 *    Microphone microphone = new Microphone();
 *    microphone.listen(new MicrophoneListener() {
 *      protected void onBufferFull(short[] buffer) {
 *        // ...
 *      }
 *    });
 *    microphone.stop();
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Microphone {

	/**
	 * @param	sampleRate	The sample rate at which the audio should be
	 * recorded
	 */
	public Microphone(long sampleRate) throws MicException {
		physicalMicrophone = getListener(
				(int) sampleRate,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.CHANNEL_IN_MONO
		);
		if (physicalMicrophone.getState() != AudioRecord.STATE_INITIALIZED) {
			throw new MicException("Microphone failed to initialize");
		};
		initializeBuffer();
	}

	public void release() {
		physicalMicrophone.release();
	}

	/** Accessors for sample rate, audio format channel configuration  */
	public int getSampleRate() { return physicalMicrophone.getSampleRate(); }
	public int getAudioFormat() { return physicalMicrophone.getAudioFormat(); }
	public int getChannelConfiguration() { 
			return physicalMicrophone.getChannelConfiguration();
	}

	/** Start listening. */
	public void listen(final MicrophoneListener callback) {
		// If there is already a thread listening then kill it and ensure it's
		// dead before creating a new thread.
		if (t != null) {
			t.interrupt();
			while (t.isAlive()) {}
		}

		// Simply reads and reads...
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				read(callback);
			}
		});
		t.start();
	}
  
	/** Stop listening to the microphone. */
	public void stop() throws MicException {
		physicalMicrophone.stop();
		if (physicalMicrophone.getState() != AudioRecord.RECORDSTATE_STOPPED) {
			throw new MicException("Failed to stop the microphone.");
		}
	}

	/**
	 * The exception class used when initialization or stopping fails.
	 */
	public static class MicException extends Exception {
		public MicException(String message) {
			super(message);
		}
	}

	/**
	 * Tries to get a listening device for the built-in/external microphone.
	 *
	 * Note: It converts the Android parameters into
	 * parameters that are useful for AudioRecord.
	 */
	private static AudioRecord getListener(
			int sampleRate, int audioFormat, int channelConfig) {

		// Sample size.
		int sampleSize;
		if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
			sampleSize = 16;
		} else {
			sampleSize = 8;
		}

		// Channels.
		int numberOfChannels;
		if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
			numberOfChannels = 1;
		} else {
			numberOfChannels = 2;
		}
		
		// Calculate buffer size.
		/** The period used for callbacks to onBufferFull. */
		int framePeriod = sampleRate * 120 / 1000;
		/** The buffer needed for the above period */
		int bufferSize = framePeriod * 2 * sampleSize * numberOfChannels / 8;
		
		return new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	/** Read from the listener's buffer and call the callback. */
	private void read(MicrophoneListener callback) {
		physicalMicrophone.startRecording();

		// Wait until something is heard.
		while (true) {
			if (physicalMicrophone.read(buffer, 0, buffer.length) <= 0) {
				break;
			}

			if (Thread.interrupted()) {
				return;
			}

			// Hand the callback a copy of the buffer.
			if (callback != null) {
				callback.onBufferFull(Arrays.copyOf(buffer, buffer.length));
			}
		}
	}

	/**
	 * The buffer is duration-constant. Length equals a certain time.
	 * We get about 44 buffers per second.
	 */
	private void initializeBuffer() {
		this.buffer = new short[getBufferSize()];
	}
	private int getBufferSize() {
		return Math.round(1000f*physicalMicrophone.getSampleRate()/44100);
	}

	private Thread t;

	/** Microphone buffer used to ferry samples to a PCM based file/consumer.*/
	private short[] buffer;

	/** AudioRecord listens to the microphone */
	private AudioRecord physicalMicrophone;
}
