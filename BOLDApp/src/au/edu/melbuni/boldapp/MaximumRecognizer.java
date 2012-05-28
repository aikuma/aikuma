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
	public boolean isSilence(byte[] buffer) {
		int reading = getMaximumAmplitude(buffer);
		
		return reading < silenceThreshold;
	}
	
	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = getMaximumAmplitude(buffer);
		
		return reading > speechThreshold;
	}

	protected int getMaximumAmplitude(byte[] buffer) {
		short maxValue = 0;
		
		for (int i = 0; i < buffer.length / 2; i++) {
			short value = getShort(buffer[i*2],
					buffer[i*2+1]);
			if (value > maxValue) {
				maxValue = value;
			}
		}
	
		return maxValue;
	}
	
	// TODO Remove duplicate code.
	//
	private short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}
}
