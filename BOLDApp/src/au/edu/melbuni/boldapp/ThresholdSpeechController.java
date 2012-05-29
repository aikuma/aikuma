package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.listeners.OnSpeechListener;

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

	OnSpeechListener speechListener;
	Player player;
	PCMWriter wavFile;

	BufferingThresholdSpeechAnalyzer speechAnalyzer;
	
	boolean recording = false;

	public ThresholdSpeechController(OnSpeechListener speechListener) {
		this.speechListener = speechListener != null ? speechListener : new OnSpeechListener() {
			
			@Override
			public void onSpeech() {
				
			}
			
			@Override
			public void onSilence() {
				
			}
		};
		
		player = Bundler.getPlayer();

		// TODO Change explicit filename.
		//
		wavFile = PCMWriter.getInstance("respeaking.wav",
				listener.getSampleRate(), listener.getChannelConfiguration(),
				listener.getAudioFormat());

		speechAnalyzer = new BufferingThresholdSpeechAnalyzer(44, 3);
	}

	public void listen(String fileName, OnCompletionListener completionListener) {
		super.listen(fileName, completionListener);
		player.startPlaying(fileName, completionListener);
		wavFile.prepare();
	}

	public void stop() {
		wavFile.close();
		player.stopPlaying();
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
		
		speechListener.onSilence();
		recording = false;
		
		// recorder.stopRecording();
		// recordingSegments.stopRecording(recorder);

//		player.rewind(1000 * (3 / (listener.getSampleRate() / 1000)));
		player.rewind(100);
		// player.rampUp(500);
		player.resume();
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();
		speechListener.onSpeech();
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
		// TODO This probably gets called too often.
		//
		if (justChanged) {
			switchToRecord();
		}
		wavFile.write(buffer);
	}

}