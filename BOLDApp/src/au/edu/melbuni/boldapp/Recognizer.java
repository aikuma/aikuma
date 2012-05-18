package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.models.Segments;


/*
 * A Recognizer tries to recognize silence or talk from a user and starts and stops
 * his player and recorder appropriately.
 * 
 * Note: Used for respeaking.
 */
public class Recognizer extends SpeechController {

	Player player;
	Recorder recorder;
	
	Segments recordingSegments;
	
	int silenceTriggers = 0;
	int speechTriggers = 0;
	protected boolean silenceTriggered = true;
	protected boolean speechTriggered = false;
	int silenceTriggerAmount = 5;
	int speechTriggerAmount = 2;

	public Recognizer() {
		player = Bundler.getPlayer();
		recorder = Bundler.getRecorder();
		
		recordingSegments = new Segments();
	}
	
	public void listen(String fileName, OnCompletionListener completionListener) {
		super.listen(fileName, completionListener);
		player.startPlaying(fileName, completionListener);
	}

	public void stop() {
		player.stopPlaying();
		super.stop();
	}

	/*
	 * Switches the mode to play mode.
	 */
	protected void switchToPlay() {
		// TODO Beep!
		//
//		Sounds.beepbeep();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
//		recorder.stopRecording();
//		recordingSegments.stopRecording(recorder);
		
		player.rewind(100);
		// player.rampUp(500);
		player.resume();
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();
		
//		recordingSegments.startRecording(recorder, "demo");
//		recorder.startRecording("test" + current++); // FIXME Make this dynamic!
	}

	public void onBufferFull(short[] buffer) {
		int reading = getMaxAmplitude(buffer);

		// Check if we need to stop.
		//
		if (isCumulativeSilence(reading)) {
			if (silenceTriggered) {
				return;
			}
			switchToPlay();
			silenceTriggered = true;
			speechTriggered = false;
		} else {
			if (isCumulativeSpeech(reading)) {
				if (speechTriggered) {
					return;
				}
				switchToRecord();
				speechTriggered = true;
				silenceTriggered = false;
			} // else just continue doing what it does.
		}
	}

	public boolean isCumulativeSilence(int reading) {
		if (isSilence(reading)) {
			silenceTriggers++;
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}

	public boolean isCumulativeSpeech(int reading) {
		if (isSpeech(reading)) {
			speechTriggers++;
		} else {
			speechTriggers = 0;
		}
		return speechTriggers > speechTriggerAmount;
	}

}
