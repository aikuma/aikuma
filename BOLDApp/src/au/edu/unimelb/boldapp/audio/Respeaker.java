package au.edu.unimelb.boldapp.audio;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Respeaker extends Recorder {

	/**
	 * The writer used to write the sample mappings file
	 */
	private BufferedWriter writer;

	/**
	 * Indicates whether the recording has finished playing
	 */
	private boolean finishedPlaying;

	/** Player to play the original with. */
	public Player player;

	/**
	 * finishedPlaying mutator
	 */
	 public void setFinishedPlaying(boolean finishedPlaying) {
	 	this.finishedPlaying = finishedPlaying;
	 }

	/**
	 * finishedPlaying accessor
	 */
	public boolean getFinishedPlaying() {
		return this.finishedPlaying;
	}

	/** Default constructor. */
	public Respeaker() {
		super(new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(32, 32)));
		setFinishedPlaying(false);
		this.player = new Player();
	}
  
	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename,
			String samplesFilename) {
		player.prepare(sourceFilename);
		super.prepare(targetFilename);
		try {
			writer = new BufferedWriter(new FileWriter(samplesFilename));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void listen() {
		super.listen();
		player.play();
		Log.i("samples", "playing_l\t\t" + player.getCurrentSample() + "," +
				file.getCurrentSample());
		try {
			writer.write(player.getCurrentSample()+",");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To listen to the final piece of annotation after playing of the original
	 * has been completed.
	 */
	public void listenAfterFinishedPlaying() {
		super.listen();
	}

	@Override
	public void stop() {
		super.stop();
		player.stop();
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		Log.i("samples", "playing\t\t" + player.getCurrentSample() + "," +
				file.getCurrentSample());
		try {
			writer.write(player.getCurrentSample()+",");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Switches the mode to record mode. */
	protected void switchToRecord() {
		player.pause();
	}

	@Override
	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			Log.i("samples", "audio\t\t" + player.getCurrentSample() + "," +
					file.getCurrentSample());
			try {
				writer.write(file.getCurrentSample()+"\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			switchToRecord();
		}
		file.write(buffer);
	}

	@Override
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			//Log.i("samples", "silence " + file.getCurrentSample());
			//If the recording has finished playing and we're just annotating
			//at the end, then we're finished and can stop the respeaking.
			if (getFinishedPlaying()) {
				Log.i("samples", "finishedPlaying");
				super.stop();
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				switchToPlay();
			}
		}
	}
}
