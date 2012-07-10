package au.edu.melbuni.boldapp;

public class AverageRecognizer extends Recognizer {

	protected int silenceThreshold;
	protected int speechThreshold;

	public AverageRecognizer() {
		this(6, 6);
	}

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

	@Override
	public boolean isSilence(short[] buffer) {
		int reading = getAverageAmplitude(buffer);
		
		return reading < silenceThreshold;
	}

	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = getAverageAmplitude(buffer);

		return reading > speechThreshold;
	}

	public int getAverageAmplitude(short[] buffer) {
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
