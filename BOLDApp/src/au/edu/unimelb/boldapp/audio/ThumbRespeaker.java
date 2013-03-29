package au.edu.unimelb.aikuma.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.util.Log;
import au.edu.unimelb.aikuma.audio.Player;
import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;
import au.edu.unimelb.aikuma.FileIO;
import au.edu.unimelb.aikuma.audio.NewSegments.Segment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeaker {

	public ThumbRespeaker() {
		recorder = new Recorder();
		player = new Player();
		segments = new NewSegments();
		setFinishedPlaying(false);
		playThroughSpeaker();
	}

	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename,
			String mappingFilename) {
		player.prepare(sourceFilename);
		recorder.prepare(targetFilename);
		this.mappingFilename = mappingFilename;
	}

	public void playOriginal() {
		// If we have already specified an end of the segment then we're
		// starting a new one. Otherwise just continue with the old
		// originalStartOfSegment
		if (originalStartOfSegment == null || originalEndOfSegment != null) {
			originalStartOfSegment = player.getCurrentSample();
		}
		Log.i("ThumbRespeaking", "originalStartOfSegment " + originalStartOfSegment);
		player.play();
	}

	public void pauseOriginal() {
		player.pause();
	}

	public void recordRespeaking() {
		originalEndOfSegment = player.getCurrentSample();
		Log.i("ThumbRespeaking", "originalEndOfSegment " + originalEndOfSegment);
		respeakingStartOfSegment = recorder.getCurrentSample();
		Log.i("ThumbRespeaking", "respeakingStartOfSegment " +
				respeakingStartOfSegment);
		recorder.listen();
	}

	public void pauseRespeaking() {
		recorder.pause();
		respeakingEndOfSegment = recorder.getCurrentSample();
		Log.i("ThumbRespeaking", "respeakingEndOfSegment " + respeakingEndOfSegment);
		storeSegmentEntry();
	}

	private void storeSegmentEntry() {
		Segment originalSegment;
		try {
			originalSegment = new Segment(originalStartOfSegment,
					originalEndOfSegment);
		} catch (IllegalArgumentException e) {
			// This could only have happened if no original had been recorded at all.
			originalSegment = new Segment(0l, 0l);
		}
		Segment respeakingSegment = new Segment(respeakingStartOfSegment,
				respeakingEndOfSegment);
		segments.put(originalSegment, respeakingSegment);
		originalStartOfSegment = player.getCurrentSample();
		originalEndOfSegment = null;
		respeakingStartOfSegment = recorder.getCurrentSample();
		respeakingEndOfSegment = null;
	}

	public void stop() {
		recorder.stop();
		player.stop();
		try {
			segments.write(new File(mappingFilename));
			Log.i("ThumbRespeaking", segments.toString());
		} catch (IOException e) {
			// Couldn't write mapping. Oh well!
		}
	}

	public void playThroughSpeaker() {
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	/** finishedPlaying mutator */
	 public void setFinishedPlaying(boolean finishedPlaying) {
	 	this.finishedPlaying = finishedPlaying;
	 }

	/** finishedPlaying accessor */
	public boolean getFinishedPlaying() {
		return this.finishedPlaying;
	}

	/** Player to play the original with. */
	private Player player;

	public void setOnCompletionListener(OnCompletionListener ocl) {
		player.setOnCompletionListener(ocl);
	}

	/** The recorder used to get respeaking data. */
	private Recorder recorder;

	/** The segment mapping between the original and the respeaking. */
	private NewSegments segments;

	/**
	 * Temporarily store the boundaries of segments before being put in
	 * segments */
	private Long originalStartOfSegment;
	private Long originalEndOfSegment;
	private Long respeakingStartOfSegment;
	private Long respeakingEndOfSegment;

	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying;

	/** The name of the mapping file */
	private String mappingFilename;
}
