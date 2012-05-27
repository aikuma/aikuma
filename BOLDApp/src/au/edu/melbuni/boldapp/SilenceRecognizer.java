package au.edu.melbuni.boldapp;

public class SilenceRecognizer {
	
	protected int silenceThreshold;
	
	public SilenceRecognizer() {
		this(6);
	}
	
	public SilenceRecognizer(int divisor) {
		int maxAmplitude = 32768; // MediaRecorder.getAudioSourceMax();
		                          // TODO Make dynamic depending on phone.
		this.silenceThreshold = maxAmplitude / divisor; // Silence is less than 1/n of max
											       // amplitude.
	}
	
	public boolean isSilence(int reading) {
		return reading < silenceThreshold;
	}

}
