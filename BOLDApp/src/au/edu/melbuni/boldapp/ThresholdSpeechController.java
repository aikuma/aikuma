package au.edu.melbuni.boldapp;

import org.apache.commons.net.ntp.TimeStamp;

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

	int BUFFERS = 4;
	short[][] lastBuffers = new short[BUFFERS][0];
	int currentBuffer = 0;
	
	boolean recording = false;

	public ThresholdSpeechController() {
		player = Bundler.getPlayer();

		// TODO Change explicit filename.
		//
		wavFile = PCMWriter.getInstance("respeaking.wav",
				listener.getSampleRate(), listener.getChannelConfiguration(),
				listener.getAudioFormat());

		speechAnalyzer = new ThresholdSpeechAnalyzer(44, BUFFERS);
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

		recording = false;
		
		// recorder.stopRecording();
		// recordingSegments.stopRecording(recorder);

		player.rewind(1000 * (BUFFERS / (listener.getSampleRate() / 1000)));
		// player.rampUp(500);
		player.resume();
	}

	/*
	 * Switches the mode to record mode.
	 */
	protected void switchToRecord() {
		player.pause();
		recording = true;
		
		// recordingSegments.startRecording(recorder, "demo");
		// recorder.startRecording("test" + current++); // FIXME Make this
		// dynamic!
	}

	public void onBufferFull(short[] buffer) {
		speechAnalyzer.analyze(this, buffer); // This will call back
												// silenceTriggered and
												// speechTriggered.
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToPlay();
		}
		clearBuffers();
	}

	// TODO Rewrite all!
	//
	public void speechTriggered(short[] buffer, boolean justChanged) {
		// TODO This probably gets called too often.
		//
		if (justChanged) {
			switchToRecord();
			wavFile.write(new byte[buffer.length * 30]); // Empty preamble.
			for (int i = 0; i < BUFFERS; i++) {
				short[] current = lastBuffers[(i + currentBuffer + 1) % BUFFERS];
//				if (current.length > 0) { wavFile.write(current); }
			}
			clearBuffers();
		}
		wavFile.write(buffer);
//		LogWriter.log(TimeStamp.getCurrentTime().toString() + ": written " + buffer.length + " bytes.");
	}

	@Override
	public void neitherTriggered(short[] buffer) {
		shiftBuffer(buffer);
		if (recording) {
			speechTriggered(buffer, false);
		}
	}

	protected void shiftBuffer(short[] buffer) {
		currentBuffer += 1;
		currentBuffer = currentBuffer % BUFFERS;
		lastBuffers[currentBuffer] = buffer.clone();
	}

	protected void clearBuffers() {
		for (int i = 0; i < BUFFERS; i++) {
			lastBuffers[i] = new short[]{};
		}
	}

}
