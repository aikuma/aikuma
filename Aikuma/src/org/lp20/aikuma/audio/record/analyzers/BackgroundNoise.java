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
	
	protected Noise thresholder;
	protected Microphone microphone;
	protected float factor;
	
	public BackgroundNoise(int duration, long sampleRate) throws MicException {
		this.thresholder = new Noise(duration);
		this.microphone = new Microphone(sampleRate);
		this.factor = 1.5f;
	}
	
	/**
	 * Tries to find a threshold value.
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
	
	public void stop() throws MicException {
		microphone.stop();
	}

}
