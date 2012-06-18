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

//	boolean recording = false;

	public ThresholdSpeechController() {
		player = Bundler.getPlayer();

		wavFile = PCMWriter.getInstance(listener.getSampleRate(),
				listener.getChannelConfiguration(), listener.getAudioFormat());

		speechAnalyzer = new BufferingThresholdSpeechAnalyzer(88, 3);
	}

	public void listen(String sourceFilename, String targetFilename,
			final OnCompletionListener completionListener) {
		super.listen(sourceFilename, targetFilename, completionListener);
		player.startPlaying(sourceFilename, "", new OnCompletionListener() {
			@Override
			public void onCompletion(Sounder sounder) {
				stop();
				completionListener.onCompletion(sounder);
			}
		});
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
		
		// TODO Move into beepbeep if possible.
		//
//		Thread t = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Sounds.beepbeep();
//			}
//		});
//		t.start();

		// speechListener.onSilence();
//		recording = false;

		// recorder.stopRecording();
		// recordingSegments.stopRecording(recorder);

		// rewind(1000 * (8 / (listener.getSampleRate() / 1000)));
		rewind(650);
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
//		recording = true;
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

	public void speechTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		wavFile.write(buffer);
	}

}