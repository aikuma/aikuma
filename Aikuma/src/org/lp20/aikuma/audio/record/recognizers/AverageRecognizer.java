package org.lp20.aikuma.audio.record.recognizers;

import android.util.Log;
import org.lp20.aikuma.audio.Processor;
import org.lp20.aikuma.audio.record.recognizers.Recognizer;

/** 
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class AverageRecognizer extends Recognizer {
  
	protected int silenceThreshold;
	protected int speechThreshold;
  protected Processor processor = new Processor();
  
  //   /** Default Constructor.
  //    *
  //    *  Default silence is less than 1/32 of the maximum.
  //    *  Default speech is more than 1/32 of the maximum.
  //    */
  public AverageRecognizer() {
    this(32768/32, 32768/32); // MediaRecorder.getAudioSourceMax();
  }
  
  /** Constructor.
   *
   *  @param silenceDivisor Silence is less than 1/silenceDivisor of the maximum.
   *  @param speechDivisor  Speech is more than 1/speechDivisor of the maximum.
   */
	public AverageRecognizer(int silenceThreshold, int speechThreshold) {
		// Silence is less than
		// 1/n of max amplitude.
		//
		this.silenceThreshold = silenceThreshold;

		// Speech is more than 1/m of max amplitude.
		//
		this.speechThreshold = speechThreshold;
	}
  
  /** Is the given buffer silent?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSilence(short[] buffer) {
		int reading = processor.getAverage(buffer);
		Log.i("sound", "reading: " + reading + ", silenceThreshold: " +
				silenceThreshold);
		return reading < silenceThreshold;
	}

  /** Is the given buffer speech?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = processor.getAverage(buffer);

		Log.i("sound", "silent reading: " + reading + ", silenceThreshold: " +
				silenceThreshold);
		return reading > speechThreshold;
	}
}
