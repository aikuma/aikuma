/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.analyzers;

import org.lp20.aikuma.audio.record.Noise;

/**
 * Interface for anything that listens to background noise data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface BackgroundNoiseListener {

	/**
	 * Quality will be a negative value getting closer and closer to 0 (higher is better).
	 *
	 * @param	information	Information about the background noise.
	 */
	public void noiseLevelQualityUpdated(Noise.Information information);
	
	/**
	 * Level will be a positive value; This will be called once when it's found.
	 *
	 * @param	information	Information about the background noise.
	 */
	public void noiseLevelFound(Noise.Information information);

}
