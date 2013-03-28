package au.edu.unimelb.aikuma.audio.thresholders;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.Processor;

/** 
 * Tries to extract the level of background noise
 * from the given buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class Noise {
	
	protected Processor processor = new Processor();
	protected int[] averages;
	protected int currentAverage = -1;
	protected float currentQuality;
	
	public Noise() {
		this(20);
	}
	
	public Noise(int windowSize) {
		this.averages = new int[windowSize];
		this.currentQuality = -1;
	}
	
	// Container class for information regarding background noise.
	//
	public class Information {
		
		private int average;
		private int minimum;
		private int maximum;
		private float quality;
		
		public Information(int average, int minimum, int maximum, float quality) {
			this.average = average;
			this.minimum = minimum;
			this.maximum = maximum;
			this.quality = quality;
		}
		
		public int getRecommendedRecordingLevel() {
			return (int) Math.round(getAverage()*1.5);
		}
		
		public int getAverage() { return this.average; }
		public int getMinimum() { return this.minimum; }
		public int getMaximum() { return this.maximum; }
		public float getQuality() { return this.quality; } // Negative. The closer to 0, the higher the quality. 
		
	}
	
	/**
	 * @return A positive number if threshold is found, else if not.
	 */
	public Information getInformation(short[] buffer) {
		addBuffer(buffer);
		float average = 0;
		int min = 0;
		int max = 0;
		float quality = -100;
		if (isBufferFilled()) {
			average = getAverage();
			min = getMin();
			max = getMax();
			
			if (average > 0) {
				// If there are no exceptional values, we return a threshold.
				//
				float minThreshold = average*0.8f;
				float maxThreshold = average*1.3f;
				if (minThreshold <= min && max <= maxThreshold) {
					quality = 0;
				} else {
					quality = -(minThreshold/min)-(max/maxThreshold);
				}
			}
		}
		Log.i("getInformation", " " + average + " " + min + " " + max);
		return new Information(Math.round(average), min, max, quality);
	}
	
	protected boolean isBufferFilled() {
		for (int i = 0; i < averages.length; i++) {
			if (averages[i] == 0) { return false; }
		}
		return true;
	}
	
	protected void addBuffer(short[] buffer) {
		currentAverage++;
		if (currentAverage >= averages.length) { currentAverage = 0; }
		averages[currentAverage] = processor.getAverage(buffer);
	}
	
	protected float getAverage() {
		float sum = 0;
		for (int i = 0; i < averages.length; i++) {
			float average = averages[i];
			if (average == 0) {
				return -1;
			} else {
				sum += averages[i];
			}
		}
		Log.i("Current average: ", "" + (sum / averages.length));
		return sum / averages.length;
	};
	
	protected int getMax() {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < averages.length; i++) {
			if (averages[i] > max) {
				max = averages[i];
			}
		}
		return max;
	};
	
	protected int getMin() {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < averages.length; i++) {
			if (averages[i] < min) {
				min = averages[i];
			}
		}
		return min;
	};

}