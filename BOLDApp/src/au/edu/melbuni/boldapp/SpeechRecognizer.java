package au.edu.melbuni.boldapp;

public class SpeechRecognizer {
	
	protected int speechThreshold;
	
	public SpeechRecognizer() {
		this(6);
	}
	
	public SpeechRecognizer(int divisor) {
		int maxAmplitude = 32768; // MediaRecorder.getAudioSourceMax();
		                          // TODO Make dynamic depending on phone.
		speechThreshold = maxAmplitude / divisor; // Speech is more than 1/m of max
												  // amplitude.
	}
	
	public boolean isSpeech(int reading) {
		return reading > speechThreshold;
	}

}