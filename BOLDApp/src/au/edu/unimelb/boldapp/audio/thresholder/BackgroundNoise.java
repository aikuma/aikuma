package au.edu.unimelb.aikuma.audio.thresholder;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.Processor;

/** 
 * Tries to extract the level of background noise
 * from the given buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class BackgroundNoise {
  
  protected Processor processor = new Processor();
  protected short[] averages;
  protected short currentAverage;
  
  public BackgroundNoise() {
    this(20);
  }
  
  public BackgroundNoise(int windowSize) {
    this.averages = new short[windowSize];
  }
  
  /**
   * @return A positive number if threshold is found, else if not.
   */
  public short getThreshold(short[] buffer) {
    short average = getAverage();
    if (average > 0) {
      // If there are no exceptional values, we return a threshold.
      //
      if (getMax() <= average*1.1 && getMin() >= average*0.9) {
        return average;
      }
    }
    return -1;
  }
  
  protected short getAverage() {
    short sum = 0;
    for (int i = 0; i < averages.length; i++) {
      short average = averages[i];
      if (average == 0) {
        return -1;
      } else {
        sum += averages[i];
      }
    }
    Log.i("Current average: ", "" + (sum / averages.length));
    return (short) (sum / averages.length);
  };
  
  protected short getMax() {
    short max = Short.MIN_VALUE;
    for (int i = 0; i < averages.length; i++) {
      if (averages[i] > max) {
        max = averages[i];
      }
    }
    return max;
  };
  
  protected short getMin() {
    short min = Short.MAX_VALUE;
    for (int i = 0; i < averages.length; i++) {
      if (averages[i] < min) {
        min = averages[i];
      }
    }
    return min;
  };

}