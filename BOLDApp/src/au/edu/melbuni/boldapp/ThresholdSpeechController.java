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

	// OnSpeechListener speechListener;
	Player player;
	PCMWriter wavFile;

	BufferingThresholdSpeechAnalyzer speechAnalyzer;

	boolean recording = false;

	public ThresholdSpeechController() {
		player = Bundler.getPlayer();
		
		wavFile = PCMWriter.getInstance(listener.getSampleRate(),
				listener.getChannelConfiguration(), listener.getAudioFormat());

		speechAnalyzer = new BufferingThresholdSpeechAnalyzer(88, 3);
	}

	public void listen(String sourceFilename, String targetFilename, OnCompletionListener completionListener) {
		super.listen(sourceFilename, targetFilename, completionListener);
		player.startPlaying(sourceFilename, "", completionListener);
		wavFile.prepare(targetFilename);
	}

	public void stop() {
		super.stop();
		wavFile.close();
		player.stopPlaying();
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

		// speechListener.onSilence();
		recording = false;

		// recorder.stopRecording();
		// recordingSegments.stopRecording(recorder);

		// player.rewind(1000 * (3 / (listener.getSampleRate() / 1000)));
		rewind(166);
		// player.rampUp(500);
		player.resume();
	}

	public void rewind(int miliseconds) {
		player.rewind(miliseconds);
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();
		// speechListener.onSpeech();
		recording = true;

		// recordingSegments.startRecording(recorder, "demo");
		// recorder.startRecording("test" + current++); // FIXME Make this
		// dynamic!
	}

	public void onBufferFull(short[] buffer) {
		// This will call back silenceTriggered and speechTriggered.
		//
		speechAnalyzer.analyze(this, buffer);
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToPlay();
		}
	}

	// TODO Rewrite all!
	//
	public void speechTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		wavFile.write(buffer);
	}

}