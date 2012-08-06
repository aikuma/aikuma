package au.edu.unimelb.boldapp.audio;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import au.edu.unimelb.boldapp.audio.analyzers.Analyzer;
import au.edu.unimelb.boldapp.audio.analyzers.ThresholdSpeechAnalyzer;

/** Respeaker used to get input from eg. a microphone and
 *  output into a file. In addition, it also 
 * 
 *  Usage:
 *    Respeaker respeaker = new Respeaker();
 *    respeaker.listen("/mnt/sdcard/bold/recordings/target_file.wav")
 *    respeaker.pause();
 *    respeaker.resume();
 *    respeaker.stop();
 *
 *  Note that stopping the respeaker closes and finalizes the WAV file.
 */
public class Respeaker extends Recorder {

	/** Default constructor. */
	public Recorder() {
		this(new ThresholdSpeechAnalyzer(88, 3, new AverageRecognizer(32, 32)));
	}

	/** Start listening. */
  @Override
	public void listen(String targetFilename) {
		
	}

	/** Stop listening to the microphone and close the file.
	 *
	 * Note: Once stopped you cannot restart the recorder.
	 */
  @Override
	public void stop() {
		super();
	}

	/** Pause listening to the microphone. */
  @Override
	public void pause() {
		super();
	}
  
  /** Rewinds the player. */
	public void rewind(int miliseconds) {
		player.rewind(miliseconds);
	}
  
	/*
	 * Switches the mode to play mode.
   * TODO Play a quick beep to inform user.
	 */
	protected void switchToPlay() {
		rewind(650);
		player.resume();
	}

	/** Switches the mode to record mode. */
	protected void switchToRecord() {
		player.pause();
	}
  
  @Override
	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		file.write(buffer);
	}

  @Override
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToPlay();
		}
	}
}
