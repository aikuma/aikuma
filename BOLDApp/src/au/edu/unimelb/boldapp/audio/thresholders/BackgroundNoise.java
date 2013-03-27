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
  protected float currentQuality;
  
  public BackgroundNoise() {
    this(20);
  }
  
  public BackgroundNoise(int windowSize) {
    this.averages = new int[windowSize];
    this.currentQuality = -1;
  }
  
  /**
   * @return A positive number if threshold is found, else if not.
   */
  public float getThreshold(short[] buffer) {
    addBuffer(buffer);
    if (isBufferFilled()) {
      float average = getAverage();
      if (average > 0) {
        // If there are no exceptional values, we return a threshold.
        //
        int min = getMin();
        float minThreshold = average*0.8f;
        int max = getMax();
        float maxThreshold = average*1.3f;
        if (minThreshold <= min && max <= maxThreshold) {
          return average;
        } else {
          currentQuality = -(minThreshold/min)-(max/maxThreshold);
        }
      }
    }
    return currentQuality;
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