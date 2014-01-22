/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.analyzers;

import android.util.Log;
import org.lp20.aikuma.audio.record.Microphone;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.MicrophoneListener;
import org.lp20.aikuma.audio.Processor;
import org.lp20.aikuma.audio.record.Noise;
import org.lp20.aikuma.audio.record.analyzers.BackgroundNoiseListener;

/** 
 * Tries to extract the level of background noise
 * from the given buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class BackgroundNoise {

	/** The thing that extracts the level of background noise from the buffers*/
	protected Noise thresholder;
	/** The microphone used to gather the noise data. */
	protected Microphone microphone;

	/**
	 * Constructor
	 *
	 * @param	duration	The duration of the noise
	 * @param	sampleRate	The sample rate of the noise.
	 * @throws	MicException	If there is an issue creating the microphone.
	 */
	public BackgroundNoise(int duration, long sampleRate) throws MicException {
		this.thresholder = new Noise(duration);
		this.microphone = new Microphone(sampleRate);
	}
	
	/**
	 * Tries to find a meaningful threshold value to define speech with.
	 *
	 * @param	listener	The thing that will listen to the background noise.
	 */
	public void getThreshold(final BackgroundNoiseListener listener) {
		// Try finding a stable background noise.
		//
		microphone.listen(new MicrophoneListener() {
			public void onBufferFull(short[] buffer) {
				Noise.Information information = BackgroundNoise.this.thresholder.getInformation(buffer);
				listener.noiseLevelQualityUpdated(information);
				if (information.getQuality() >= 0) {
					try {
						microphone.stop();
					} catch (MicException e) {
						//Not much can be done.
					}
					listener.noiseLevelFound(information);
				}
			}
		});
	}

	/**
	 * Stops the microphone.
	 *
	 * @throws	MicException	If there is an issue stopping the microphone.
	 */
	public void stop() throws MicException {
		microphone.stop();
	}

}
