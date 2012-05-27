package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

/*
 * A Recognizer tries to recognize silence or talk from a user and starts and stops
 * his player (and recorder) appropriately.
 * 
 * 
 * TODO Rename Ð it's not the thresholding, but the playing/stopping that makes this class.
 * 
 * Note: Used for respeaking.
 */
public class ThresholdSpeechController extends SpeechController {

	Player player;
	PCMWriter wavFile;

	ThresholdSpeechAnalyzer speechAnalyzer;

	boolean changedTrigger = true;

	// Segments recordingSegments;

	public ThresholdSpeechController() {
		player = Bundler.getPlayer();

		// TODO Change explicit filename.
		//
		wavFile = PCMWriter.getInstance("respeaking.wav",
				listener.getSampleRate(), listener.getChannelConfiguration(),
				listener.getAudioFormat());

		speechAnalyzer = new ThresholdSpeechAnalyzer(5, 2);

		// recordingSegments = new Segments();
	}

	public void listen(String fileName, OnCompletionListener completionListener) {
		super.listen(fileName, completionListener);
		player.startPlaying(fileName, completionListener);
		wavFile.prepare();
	}

	public void stop() {
		player.stopPlaying();
		wavFile.close();
		super.stop();
	}

	/*
	 * Switches the mode to play mode.
	 */
	protected void switchToPlay() {
		// TODO Beep!
		//
		// Sounds.beepbeep();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		// recorder.stopRecording();
		// recordingSegments.stopRecording(recorder);

		player.rewind(100);
		// player.rampUp(500);
		player.resume();
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();

		// recordingSegments.startRecording(recorder, "demo");
		// recorder.startRecording("test" + current++); // FIXME Make this
		// dynamic!
	}

	public void onBufferFull(short[] buffer) {
		speechAnalyzer.analyze(this, buffer); // This will call back
												// silenceTriggered and
												// speechTriggered.
	}

	public void silenceTriggered(short[] buffer, int reading, boolean justChanged) {
		if (justChanged) {
			switchToPlay();
		}
	}

	public void speechTriggered(short[] buffer, int reading, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		wavFile.write(buffer);
	}

}
