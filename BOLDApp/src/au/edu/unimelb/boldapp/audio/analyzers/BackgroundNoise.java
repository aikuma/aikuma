package au.edu.unimelb.aikuma.audio.analyzers;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.Microphone;
import au.edu.unimelb.aikuma.audio.MicrophoneListener;
import au.edu.unimelb.aikuma.audio.Processor;
import au.edu.unimelb.aikuma.audio.thresholders.Noise;

/** 
 * Tries to extract the level of background noise
 * from the given buffers.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class BackgroundNoise {
  
  protected Noise thresholder;
  protected Microphone microphone;
  protected float factor;
  
  public BackgroundNoise(int duration) {
    this.thresholder = new Noise(duration);
    this.microphone = new Microphone();
    this.factor     = 1.5f;
  }
  
  /**
   * Tries to find a threshold value.
   */
  public void getThreshold(final BackgroundNoiseListener listener) {
    // Try finding a stable background noise.
    //
    microphone.listen(new MicrophoneListener() {
      public void onBufferFull(short[] buffer) {
        Noise.Information information = BackgroundNoise.this.thresholder.getInformation(buffer);
        listener.noiseLevelQualityUpdated(information);
        if (information.getQuality() >= 0) {
          microphone.stop();
					listener.noiseLevelFound(information);
        }
      }
    });
  }

}