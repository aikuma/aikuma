package au.edu.melbuni.boldapp;

public class ThresholdSpeechAnalyzer {
	
	protected boolean silenceTriggered = true; // Start with silence, always.
	protected boolean speechTriggered = false;
	int silenceTriggers = 0;
	int speechTriggers = 0;
	int silenceTriggerAmount;
	int speechTriggerAmount;
	
	SilenceRecognizer silenceRecognizer;
	SpeechRecognizer speechRecognizer;
	
	public ThresholdSpeechAnalyzer() {
		this(5, 2);
	}
	
	public ThresholdSpeechAnalyzer(int silenceTriggerAmount, int speechTriggerAmount) {
		this.silenceTriggerAmount = silenceTriggerAmount;
		this.speechTriggerAmount = speechTriggerAmount;
		
		this.silenceRecognizer = new SilenceRecognizer();
		this.speechRecognizer = new SpeechRecognizer();
	}
	
	protected boolean isSilenceTriggered() {
		return silenceTriggered;
	}

	protected boolean isSpeechTriggered() {
		return speechTriggered;
	}
	
	protected void triggerSilence() {
		silenceTriggered = true;
		speechTriggered = false;
	}
	
	protected void triggerSpeech() {
		silenceTriggered = false;
		speechTriggered = true;
	}
	
	protected boolean doesTriggerSilence(int reading) {
		if (silenceRecognizer.isSilence(reading)) {
			silenceTriggers++;
			if (!isSilenceTriggered()) { triggerSilence(); }
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}
	
	protected boolean doesTriggerSpeech(int reading) {
		if (speechRecognizer.isSpeech(reading)) {
			speechTriggers++;
			if (!isSpeechTriggered()) { triggerSpeech(); }
		} else {
			speechTriggers = 0;
		}
		return speechTriggers > speechTriggerAmount;
	}
	
	protected int getMaxAmplitude(short[] buffer) {
		short maxValue = 0;
	
		// Check every 5th sample.
		//
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] > maxValue) {
				maxValue = buffer[i];
			}
		}
	
		return maxValue;
	}
	
	public void analyze(SpeechTriggers trigger, short[] buffer) {
		int reading = getMaxAmplitude(buffer);

		// Check if we need to callback.
		//
		if (doesTriggerSilence(reading)) {
			trigger.silenceTriggered(buffer);
		} else {
			if (doesTriggerSpeech(reading)) {
				trigger.speechTriggered(buffer);
			} // else just continue doing what it does.
		}
	}
}
