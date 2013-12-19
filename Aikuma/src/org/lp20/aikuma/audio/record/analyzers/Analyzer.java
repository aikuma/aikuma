/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.analyzers;

import org.lp20.aikuma.audio.record.AudioHandler;

/**
 * Base class for analyzers.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class Analyzer {

	/**
	 * A recorder calls this to analyze the incoming
	 * audio data.
	 *
	 * Depending on the outcome, it either calls back
	 *  * audioTriggered
	 *  * silenceTriggered
	 * where the first should be called if there is
	 * something worthwhile to record and the second
	 * if we detected only silence.
	 *
	 * @param handler Any AudioHandler.
	 * @param buffer An array of audio samples.
	 */
	public abstract void analyze(AudioHandler handler, short[] buffer);

	/**
	 * Resets the analyzer to default values.
	 */
	public abstract void reset();
}
