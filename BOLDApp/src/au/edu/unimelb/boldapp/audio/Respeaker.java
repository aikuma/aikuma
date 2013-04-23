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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Respeaker {

	public Respeaker() {
		recorder = new Recorder();
		player = new Player();
		mapper = new Mapper();
		setFinishedPlaying(false);
		playThroughSpeaker();
	}

	public Respeaker(Context context) {
		recorder = new Recorder(context);
		player = new Player();
		mapper = new Mapper();
		setFinishedPlaying(false);
		playThroughSpeaker();
	}

	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename,
			String mappingFilename) {
		player.prepare(sourceFilename);
		recorder.prepare(targetFilename);
		mapper.prepare(mappingFilename);
	}

	public void playOriginal() {
		mapper.markOriginal(player);
		player.seekTo(player.sampleToMsec(mapper.getOriginalStartSample()));
		player.play();
	}

	public void pauseOriginal() {
		player.pause();
	}

	public void recordRespeaking() {
		mapper.markRespeaking(player, recorder);
		recorder.listen();
	}

	public void pauseRespeaking() {
		recorder.pause();
		mapper.store(player, recorder);
	}

	public void stop() {
		recorder.stop();
		player.stop();
		mapper.stop();
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

	public void setOnCompletionListener(OnCompletionListener ocl) {
		player.setOnCompletionListener(ocl);
	}

	/** Player to play the original with. */
	private Player player;

	/** The recorder used to get respeaking data. */
	private Recorder recorder;
	
	/** The mapper used to store mapping data. */
	private Mapper mapper;

	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying;
}
