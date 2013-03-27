package au.edu.unimelb.aikuma.audio.analyzers;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.Microphone;
import au.edu.unimelb.aikuma.audio.MicrophoneListener;
import au.edu.unimelb.aikuma.audio.Processor;

/** 
 * Tries to extract the level of background noise
 * from the given buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class BackgroundNoise {
  
  protected au.edu.unimelb.aikuma.audio.thresholders.BackgroundNoise thresholder;
  protected Microphone microphone;
  protected float threshold = -1;
  protected float factor;
  
  public BackgroundNoise(int duration) {
    this.thresholder = new au.edu.unimelb.aikuma.audio.thresholders.BackgroundNoise(duration);
    this.microphone = new Microphone();
    this.factor     = 1.5f;
  }
  
  /**
   * Tries to find a threshold value.
   */
  public int getThreshold() {
    return getThreshold(new BackgroundNoiseQualityListener() {
      public void noiseLevelQualityUpdated(float quality) {
        // Nothing.
      }
    });
  }
  public int getThreshold(final BackgroundNoiseQualityListener listener) {
    // Try finding a stable background noise.
    //
    microphone.listen(new MicrophoneListener() {
      public void onBufferFull(short[] buffer) {
        BackgroundNoise.this.threshold = BackgroundNoise.this.thresholder.getThreshold(buffer);
        Log.i("Quality: ", "" + BackgroundNoise.this.threshold);
        listener.noiseLevelQualityUpdated(BackgroundNoise.this.threshold);
        if (BackgroundNoise.this.threshold >= 0) {
          microphone.stop();
        }
      }
    });
    
    // Blocking.
    //
    while (threshold < 0) {};
    
    // How much more sensitive?
    //
    return Math.round(threshold * factor);
  }

}