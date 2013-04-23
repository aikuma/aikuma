package au.edu.unimelb.aikuma.audio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import au.edu.unimelb.aikuma.audio.Player;
import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;

import au.edu.unimelb.aikuma.FileIO;

import au.edu.unimelb.aikuma.audio.NewSegments.Segment;

/** Respeaker used to get input from eg. a microphone and
 *  output into a file.tIn addition, it also 
 * 
 *  Usage:
 *    AudioRespeaker respeaker = new AudioRespeaker();
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
public class AudioRespeaker extends Recorder {

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

	private NewSegments segments;

	private Long originalStartOfSegment;
	private Long originalEndOfSegment;
	private Long respeakingStartOfSegment;
	private Long respeakingEndOfSegment;

	private String mappingFilename;

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

	public void setSensitivity(int threshold) {
		this.analyzer = new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(threshold, threshold));
	}

	/** Default constructor. */
	public AudioRespeaker() {
		super();
		setFinishedPlaying(false);
		this.player = new Player();
		segments = new NewSegments();
	}

	public AudioRespeaker(Context context) {
		super(context);
		setFinishedPlaying(false);
		this.player = new Player();
		segments = new NewSegments();
	}

	/** Default constructor. */
	public AudioRespeaker(ThresholdSpeechAnalyzer analyzer, boolean
			shouldPlayThroughSpeaker) {
		super(analyzer);
		setFinishedPlaying(false);
		this.player = new Player();
		if (shouldPlayThroughSpeaker) {
			this.playThroughSpeaker();
		} else {
			this.playThroughEarpiece();
		}
		segments = new NewSegments();
	}

	public AudioRespeaker(ThresholdSpeechAnalyzer analyzer, Context context) {
		super(analyzer, context);
		setFinishedPlaying(false);
		this.player = new Player();
		segments = new NewSegments();
	}

  
	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename,
			String mappingFilename) {
		player.prepare(sourceFilename);
		super.prepare(targetFilename);
		this.mappingFilename = mappingFilename;
		try {
			writer = new BufferedWriter(new FileWriter(mappingFilename + "old"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void listen() {
		super.listen();
		player.play();
		originalStartOfSegment = player.getCurrentSample();
	}

	public void listenToSpeaker() {
		super.listen();
	}

	/**
	 * To listen to the final piece of annotation after playing of the original
	 * has been completed.
	 */
	public void listenAfterFinishedPlaying() {
		super.listen();
	}

	public void play() {
		player.play();
	}

	@Override
	public void stop() {
		super.stop();
		player.stop();
		if (respeakingStartOfSegment != null) {
			Segment originalSegment = new Segment(
				originalStartOfSegment,
				originalEndOfSegment);
			Segment respeakingSegment = new Segment(
					respeakingStartOfSegment,
					file.getCurrentSample());
			segments.put(originalSegment, respeakingSegment);
		}
		try {
			Log.i("segments", "Samples file name: " + mappingFilename);
			segments.write(new File(mappingFilename));
		} catch (IOException e) {
			Log.e("segments", "Could not write mapping", e);
		}
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
		// Reset the analyzer to default values so it doesn't assume speech on
		// resuming.
		analyzer.reset();
	}

	/** Resume playing. */
	public void resume() {
		super.listen();
		originalStartOfSegment = player.getCurrentSample();
		switchToPlay();
	}

	/** Rewinds the player. */
	public void rewind(int milliseconds) {
		player.rewind(milliseconds);
	}
  
	/*
	 * Switches the mode to play mode.
	 *
	 */
	protected void switchToPlay() {
		rewind(650);
		respeakingEndOfSegment = file.getCurrentSample();
		Segment originalSegment = new Segment(
				originalStartOfSegment, originalEndOfSegment);
		Segment respeakingSegment = new Segment(
				respeakingStartOfSegment, respeakingEndOfSegment);
		segments.put(originalSegment, respeakingSegment);
		respeakingStartOfSegment = null;
		respeakingEndOfSegment = null;
		originalStartOfSegment = player.getCurrentSample();
		player.resume();
	}

	/** Switches the mode to record mode. */
	protected void switchToRecord() {
		player.pause();
		originalEndOfSegment = player.getCurrentSample();
		respeakingStartOfSegment = file.getCurrentSample();
	}

	@Override
	public void audioTriggered(short[] buffer, boolean justChanged) {
		long currentSample = file.getCurrentSample();
		long originalCurrentSample = player.getCurrentSample();
		if (justChanged) {
			try {
				writer.write(originalCurrentSample + ",");
				respeakingStartOfSegment = currentSample;
			} catch (Exception e) {
				e.printStackTrace();
			}
			switchToRecord();
		}
		file.write(buffer);
	}

	@Override
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		Log.i("Bra", "no dice.");
		long currentSample = file.getCurrentSample();
		long originalCurrentSample = player.getCurrentSample();
		if (currentSample % 10000 == 0) {
			Log.i("issue37mapping", "r: " + currentSample + " o: " +
					originalCurrentSample);
		}
		if (justChanged) {
			//If the recording has finished playing and we're just annotating
			//at the end, then we're finished and can stop the respeaking.
			try {
				Log.i("issue37mapping", "respeaking: " + currentSample);
				writer.write(currentSample + "\n");

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (getFinishedPlaying()) {
				respeakingEndOfSegment = file.getCurrentSample();
				Segment originalSegment = new Segment(
						originalStartOfSegment, originalEndOfSegment);
				Segment respeakingSegment = new Segment(
						respeakingStartOfSegment, respeakingEndOfSegment);
				Log.i("segments", "putting: " + originalStartOfSegment + "," +
						originalEndOfSegment + ":" + respeakingStartOfSegment + "," +
						respeakingEndOfSegment);
				segments.put(originalSegment, respeakingSegment);
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

	public void playThroughEarpiece() {
		player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);

	}

	public void playThroughSpeaker() {
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
}
