package au.edu.melbuni.boldapp;

public class ThresholdSpeechAnalyzer {
	
	int silenceTriggers = 0;
	int speechTriggers = 0;
	int silenceTriggerAmount;
	int speechTriggerAmount;
	
	Recognizer recognizer;
	
	public ThresholdSpeechAnalyzer() {
		this(5, 2);
	}
	
	public ThresholdSpeechAnalyzer(int silenceTriggerAmount, int speechTriggerAmount) {
		this.silenceTriggerAmount = silenceTriggerAmount;
		this.speechTriggerAmount = speechTriggerAmount;
		
		this.recognizer = new AverageRecognizer(10, 10);
	}
	
	protected boolean doesTriggerSilence(byte[] buffer) {
		if (recognizer.isSilence(buffer)) {
			silenceTriggers++;
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}
	
	protected boolean doesTriggerSpeech(byte[] buffer) {
		if (recognizer.isSpeech(buffer)) {
			speechTriggers++;
		} else {
			speechTriggers = 0;
		}
		return speechTriggers > speechTriggerAmount;
	}
	
	public void analyze(SpeechTriggers trigger, byte[] buffer) {
		// Check if we need to callback.
		//
		if (doesTriggerSilence(buffer)) {
			trigger.silenceTriggered(buffer, silenceTriggers == silenceTriggerAmount + 1);
		} else {
			if (doesTriggerSpeech(buffer)) {
				trigger.speechTriggered(buffer, speechTriggers == speechTriggerAmount + 1);
			} else {
				trigger.neitherTriggered(buffer);
			}
		}
	}
}
