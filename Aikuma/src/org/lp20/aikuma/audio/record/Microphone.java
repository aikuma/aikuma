/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import android.media.AudioFormat;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AutomaticGainControl;
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
	 * Constructor
	 *
	 * @param	sampleRate	The sample rate at which the audio should be
	 * recorded
	 * @throws	MicException	If the microphone couldn't be set up.
	 */
	public Microphone(long sampleRate) throws MicException {
		physicalMicrophone = getListener(
				(int) sampleRate,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.CHANNEL_IN_MONO
		);
		boolean acgOff = ensureAGCIsOff();
		Log.i("agc", "Is the ACG is definitely off: " + acgOff);
		if (physicalMicrophone.getState() != AudioRecord.STATE_INITIALIZED) {
			throw new MicException("Microphone failed to initialize");
		};
		initializeBuffer();
	}

	/**
	 * Ensures Automatic Gain Control is off.
	 *
	 * @return	true if ACG is guaranteed to be off; false otherwise.
	 */
	private boolean ensureAGCIsOff() {
		int audioSessionId = physicalMicrophone.getAudioSessionId();
		try {
			AutomaticGainControl agc = AutomaticGainControl.create(audioSessionId);
			if (agc == null) {
				//The device does not implement automatic gain control.
				return true;
			}
			if (agc.getEnabled()) {
				int result = agc.setEnabled(false);
				if (result == AudioEffect.SUCCESS) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		} catch (java.lang.NoSuchMethodError e) {
			// In such circumstances the device is using an API < 16, which
			// means there is no AutomaticGainControl class available. The
			// device may or may not have various filters such as AGC
			// implemented at a into the audio input path at the hardware codec
			// or multimedia DSP level. While it's possible AGC is not on, it
			// cannot be guaranteed in these circumstances.
			return false;
		}
	}
	
	/**
	 * Releases the physical microphone.
	 */
	public void release() {
		physicalMicrophone.release();
	}

	public int getSampleRate() { return physicalMicrophone.getSampleRate(); }
	public int getAudioFormat() { return physicalMicrophone.getAudioFormat(); }
	public int getChannelConfiguration() { 
			return physicalMicrophone.getChannelConfiguration();
	}

	/**
	 * Start listening.
	 *
	 * @param	callback	The callback to hand audio buffers to.
	 */
	public void listen(final MicrophoneListener callback) {
		// If there is already a thread listening then kill it and ensure it's
		// dead before creating a new thread.
		Log.i("thread", "listen");
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
  
	/**
	 * Stop listening to the microphone.
	 *
	 * @throws	MicException	If there is a problem stopping the microphone.
	 */
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
		/**
		 * Constructor
		 *
		 * @param	message	The exception message.
		 */
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
			Log.i("thread", "read");
			if (physicalMicrophone.read(buffer, 0, buffer.length) <= 0) {
				break;
			}

			if (Thread.interrupted()) {
				Log.i("thread", "interrupted");
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
		// 10/441 ~ 22.6msec
		return Math.round(1000f*physicalMicrophone.getSampleRate()/44100);
	}

	private Thread t;

	/** Microphone buffer used to ferry samples to a PCM based file/consumer.*/
	private short[] buffer;

	/** AudioRecord listens to the microphone */
	private AudioRecord physicalMicrophone;
}
