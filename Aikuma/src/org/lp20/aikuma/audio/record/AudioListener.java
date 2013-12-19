/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

/**
 * Interface for anything that listens to audio buffer data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface AudioListener {

	/**
	 * The microphone/etc. has picked up something.
	 *
	 * @param buffer An array of samples.
	 */
	public void onBufferFull(short[] buffer);

}
