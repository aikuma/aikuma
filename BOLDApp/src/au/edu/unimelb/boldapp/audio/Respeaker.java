package au.edu.unimelb.boldapp.audio;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import au.edu.unimelb.boldapp.audio.Player;
import au.edu.unimelb.boldapp.audio.analyzers.Analyzer;
import au.edu.unimelb.boldapp.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.boldapp.audio.recognizers.AverageRecognizer;

/** Respeaker used to get input from eg. a microphone and
 *  output into a file. In addition, it also 
 * 
 *  Usage:
 *    Respeaker respeaker = new Respeaker();
 *    respeaker.prepare(
 *      "/mnt/sdcard/bold/recordings/source_file.wav",
 *      "/mnt/sdcard/bold/recordings/target_file.wav"
 *    );
 *    respeaker.listen();
 *    respeaker.pause();
 *    respeaker.resume();
 *    respeaker.stop();
 *
 *  Note that stopping the respeaker closes and finalizes the WAV file.
 */
public class Respeaker extends Recorder {
  
  /** Player to play the original with. */
  protected Player player;
  
	/** Default constructor. */
  public Respeaker() {
    super(new ThresholdSpeechAnalyzer(88, 3, new AverageRecognizer(32, 32)));
    this.player = new Player();
  }
  
	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename) {
		player.prepare(sourceFilename);
		super.prepare(targetFilename);
	}

  @Override
	public void listen() {
		super.listen();
		player.play();
	}

  @Override
	public void stop() {
		super.stop();
    player.stop();
	}

	/** Pause listening to the microphone. */
  @Override
	public void pause() {
		super.pause();
    player.pause();
	}
  
	/** Resume playing. */
	public void resume() {
		super.listen();
		switchToPlay();
	}
  
  /** Rewinds the player. */
	public void rewind(int miliseconds) {
		player.rewind(miliseconds);
	}
  
	/*
	 * Switches the mode to play mode.
   *
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
