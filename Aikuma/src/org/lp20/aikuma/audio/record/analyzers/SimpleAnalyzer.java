/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.analyzers;

import org.lp20.aikuma.audio.record.AudioHandler;

/**
 * A simple dummy analyzer that always tells the AudioHandler that audio has
 * been triggered.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SimpleAnalyzer extends Analyzer {

	/**
	 * Simply always calls audioTriggered.
	 *
	 * @param handler Any AudioHandler.
	 * @param buffer An array of audio samples.
	 */
	public void analyze(AudioHandler handler, short[] buffer) {
		handler.audioTriggered(buffer, false);
	}

	/** Resets the analyzer to default values. */
	public void reset() {
	}
}
