package au.edu.unimelb.aikuma.audio.analyzers;

/**
 * Interface for anything that listens to background noise data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface BackgroundNoiseQualityListener {

	/**
	 * Quality will be a negative value getting closer and closer to 0 (higher is better).
	 */
	public void noiseLevelQualityUpdated(float quality);

}
