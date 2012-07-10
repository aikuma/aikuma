package au.edu.melbuni.boldapp;

public class MaximumRecognizer extends Recognizer {

	protected int silenceThreshold;
	protected int speechThreshold;

	public MaximumRecognizer() {
		this(6, 6);
	}

	public MaximumRecognizer(int silenceDivisor, int speechDivisor) {
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

	@Override
	public boolean isSilence(short[] buffer) {
		int reading = getMaximumAmplitude(buffer);
		
		return reading < silenceThreshold;
	}
	
	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = getMaximumAmplitude(buffer);
		
		return reading > speechThreshold;
	}

	protected int getMaximumAmplitude(short[] buffer) {
		short maxValue = 0;
		
		for (int i = 0; i < buffer.length; i++) {
			short value = buffer[i];
			
			if (value > maxValue) {
				maxValue = value;
			}
		}
	
		return maxValue;
	}
}
