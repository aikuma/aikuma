package au.edu.melbuni.boldapp;

public class ThresholdSpeechAnalyzer {
	
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
	
	protected boolean doesTriggerSilence(int reading) {
		if (silenceRecognizer.isSilence(reading)) {
			silenceTriggers++;
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}
	
	protected boolean doesTriggerSpeech(int reading) {
		if (speechRecognizer.isSpeech(reading)) {
			speechTriggers++;
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
		
//		LogWriter.log("Reading: " + reading);

		// Check if we need to callback.
		//
		if (doesTriggerSilence(reading)) {
			trigger.silenceTriggered(buffer, reading, silenceTriggers == silenceTriggerAmount + 1);
		} else {
			if (doesTriggerSpeech(reading)) {
				trigger.speechTriggered(buffer, reading, speechTriggers == speechTriggerAmount + 1);
			} // else just continue doing what it does.
		}
	}
}
