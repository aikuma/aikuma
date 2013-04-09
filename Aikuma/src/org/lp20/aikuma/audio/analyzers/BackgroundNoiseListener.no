package au.edu.unimelb.aikuma.audio.analyzers;

import au.edu.unimelb.aikuma.audio.thresholders.Noise;

/**
 * Interface for anything that listens to background noise data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface BackgroundNoiseListener {

	/**
	 * Quality will be a negative value getting closer and closer to 0 (higher is better).
	 */
	public void noiseLevelQualityUpdated(Noise.Information information);
	
	/**
	 * Level will be a positive value. This will be called once when it's found.
	 */
	public void noiseLevelFound(Noise.Information information);

}
