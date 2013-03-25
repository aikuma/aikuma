package au.edu.unimelb.aikuma.audio.recognizers;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.Processor;
import au.edu.unimelb.aikuma.audio.recognizers.Recognizer;

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
  // public AverageRecognizer() {
  //   this(32, 32);
  // }
  
  /** Constructor.
   *
   *  @param silenceDivisor Silence is less than 1/silenceDivisor of the maximum.
   *  @param speechDivisor  Speech is more than 1/speechDivisor of the maximum.
   */
	public AverageRecognizer(int silenceThreshold, int speechThreshold) {
		// MediaRecorder.getAudioSourceMax();
		// TODO Make dynamic depending on phone.
		//
    // int maxAmplitude = 32768;

		// Silence is less than
		// 1/n of max amplitude.
		//
		this.silenceThreshold = silenceThreshold; // maxAmplitude / silenceDivisor;

		// Speech is more than 1/m of max amplitude.
		//
		this.speechThreshold = speechThreshold; // maxAmplitude / speechDivisor;
	}
  
  /** Is the given buffer silent?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSilence(short[] buffer) {
		int reading = processor.getAverage(buffer);
		
		Log.i("Bra", "is silence " + (reading < silenceThreshold));
		return reading < silenceThreshold;
	}

  /** Is the given buffer speech?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = processor.getAverage(buffer);

		return reading > speechThreshold;
	}
}
