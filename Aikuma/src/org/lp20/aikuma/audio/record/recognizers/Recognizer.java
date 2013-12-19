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

	public abstract boolean isSilence(short[] buffer);

	public abstract boolean isSpeech(short[] buffer);
	
}
