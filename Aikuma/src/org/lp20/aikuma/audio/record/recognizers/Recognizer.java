/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.recognizers;

/**
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public abstract class Recognizer {

	/**
	 * Determines whether a buffer represents silence.
	 *
	 * @param	buffer	The buffer containing the audio
	 * @return	true if silence; false otherwise
	 */
	public abstract boolean isSilence(short[] buffer);

	/**
	 * Determines whether a buffer represents speech.
	 *
	 * @param	buffer	The buffer containing the audio
	 * @return	true if speech; false otherwise
	 */
	public abstract boolean isSpeech(short[] buffer);
}
