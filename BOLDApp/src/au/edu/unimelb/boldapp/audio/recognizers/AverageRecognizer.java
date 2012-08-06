package au.edu.unimelb.boldapp.audio.recognizers;

import au.edu.unimelb.boldapp.audio.recognizers.Recognizer;

/** 
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class AverageRecognizer extends Recognizer {

	protected int silenceThreshold;
	protected int speechThreshold;
  
  /** Default Constructor.
   *
   *  Default silence is less than 1/32 of the maximum.
   *  Default speech is more than 1/32 of the maximum.
   */
	public AverageRecognizer() {
		this(32, 32);
	}
  
  /** Constructor.
   *
   *  @param silenceDivisor Silence is less than 1/silenceDivisor of the maximum.
   *  @param speechDivisor  Speech is more than 1/speechDivisor of the maximum.
   */
	public AverageRecognizer(int silenceDivisor, int speechDivisor) {
		// MediaRecorder.getAudioSourceMax();
		// TODO Make dynamic depending on phone.
		//
		int maxAmplitude = 32768;

		// Silence is less than
		// 1/n of max amplitude.
		//
		this.silenceThreshold = maxAmplitude / silenceDivisor;

		// Speech is more than 1/m of max amplitude.
		//
		this.speechThreshold = maxAmplitude / speechDivisor;
	}
  
  /** Is the given buffer silent?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSilence(short[] buffer) {
		int reading = getAverageAmplitude(buffer);
		
		return reading < silenceThreshold;
	}

  /** Is the given buffer speech?
   *
   * @param buffer The samples to check.
   */
	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = getAverageAmplitude(buffer);

		return reading > speechThreshold;
	}
  
  /** Evaluates the average amplitude.
   *
   * @return Average amplitude of the buffer.
   */
	protected int getAverageAmplitude(short[] buffer) {
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
