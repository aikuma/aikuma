/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

/**
 * Provides methods to process audio buffers - at the moment a single method to
 * determine the average amplitude.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class Processor {

	/** Evaluates the average amplitude.
	 *
	 * @param	buffer	The buffer containing the audio data.
	 * @return Average amplitude of the buffer.
	 */
	public int getAverage(short[] buffer) {
		int sum = 0;
		int amount = 0;

		for (int i = 0; i < buffer.length; i++) {
			short value = buffer[i];
			
			if (value >= 0) { 
				sum += value;
				amount += 1;
			}
		}

		return amount == 0 ? sum : sum / amount;
	}
}
