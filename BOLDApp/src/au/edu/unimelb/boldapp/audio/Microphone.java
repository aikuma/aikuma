package au.edu.unimelb.aikuma.audio;

import java.util.Arrays;
import java.util.Set;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/** A Microphone is used to get input from the physical microphone
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
 */
public class Microphone {

	private Thread t;

	/** Microphone buffer.
	 *
	 *  Used to ferry samples to a PCM based file/consumer.
	 */
	private short[] buffer;

	/** AudioRecord listens to the microphone */
	private AudioRecord physicalMicrophone;
  
	public Microphone() {
		physicalMicrophone = getListener(
				Constants.SAMPLE_RATE,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.CHANNEL_CONFIGURATION_MONO
		);
		waitForPhysicalMicrophone();
		initializeBuffer();
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
	
	public int getSampleRate() { return physicalMicrophone.getSampleRate(); }
	public int getChannelConfiguration() { return physicalMicrophone.getChannelConfiguration(); }
	public int getAudioFormat() { return physicalMicrophone.getAudioFormat(); }

	/** Start listening. */
	public void listen(final MicrophoneListener callback) {
		// If there is already a thread listening then kill it and ensure it's
		// dead before creating a new thread.
    //
		if (t != null) {
			t.interrupt();
			while (t.isAlive()) {}
		}

		// Simply reads and reads...
		//
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				read(callback);
			}
		});
		t.start();
	}
  
	/** Stop listening to the microphone. */
	public void stop() {
		physicalMicrophone.stop();
		do {
		} while (physicalMicrophone.getState() != AudioRecord.RECORDSTATE_STOPPED);
	}
	
	/** Waits for the listening device. */
	private void waitForPhysicalMicrophone() {
		do {} while (physicalMicrophone.getState() != AudioRecord.STATE_INITIALIZED);
	}
	
	/** Tries to get a listening device for the built-in/external microphone.
	 *
	 * Note: It converts the Android parameters into
	 * parameters that are useful for AudioRecord.
	 */
	private static AudioRecord getListener(
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
	
	/** Read from the listener's buffer and call the callback. */
	private void read(MicrophoneListener callback) {
		physicalMicrophone.startRecording();

		// Wait until something is heard.
    //
		while (true) {
			if (physicalMicrophone.read(buffer, 0, buffer.length) <= 0) {
				break;
			}
      
			if (Thread.interrupted()) {
				return;
			}
      
			// Hand the callback a copy of the buffer.
			//
			if (callback != null) {
        callback.onBufferFull(Arrays.copyOf(buffer, buffer.length));
      }
		}
	}
}
