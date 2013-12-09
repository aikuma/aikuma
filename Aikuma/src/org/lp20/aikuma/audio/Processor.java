package org.lp20.aikuma.audio;

/**
 * Can do basic processing on audio buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class Processor {
  
  /** Evaluates the average amplitude.
   *
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
