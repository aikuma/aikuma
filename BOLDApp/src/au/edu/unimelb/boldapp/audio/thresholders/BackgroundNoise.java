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
public class BackgroundNoise {
  
  protected Processor processor = new Processor();
  protected int[] averages;
  protected int currentAverage = -1;
  
  public BackgroundNoise() {
    this(20);
  }
  
  public BackgroundNoise(int windowSize) {
    this.averages = new int[windowSize];
  }
  
  /**
   * @return A positive number if threshold is found, else if not.
   */
  public int getThreshold(short[] buffer) {
    addBuffer(buffer);
    if (!isBufferFilled()) { return -1; }
    int average = getAverage();
    if (average > 0) {
      // If there are no exceptional values, we return a threshold.
      //
      // Log.i("max", "" + getMax());
      // Log.i("avg*1.2", "" + average*1.2);
      // Log.i("min", "" + getMin());
      // Log.i("avg*0.9", "" + average*0.9);
      if (getMax() <= average*1.3 && getMin() >= average*0.8) {
        return average;
      }
    }
    return -1;
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
  
  protected int getAverage() {
    int sum = 0;
    for (int i = 0; i < averages.length; i++) {
      int average = averages[i];
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