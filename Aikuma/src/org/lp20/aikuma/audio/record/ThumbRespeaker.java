package org.lp20.aikuma.audio.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.record.analyzers.Analyzer;
import org.lp20.aikuma.audio.record.analyzers.ThresholdSpeechAnalyzer;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.recognizers.AverageRecognizer;
import org.lp20.aikuma.audio.Player.OnCompletionListener;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.util.FileIO;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeaker {

	public ThumbRespeaker(Recording original, UUID respeakingUUID)
			throws MicException, IOException {
		recorder = new Recorder(new File(Recording.getRecordingsPath(),
				respeakingUUID + ".wav"), original.getSampleRate());
		player = new SimplePlayer(original);
		mapper = new Mapper(respeakingUUID);
		setFinishedPlaying(false);
		playThroughSpeaker();
	}

	/*
	public ThumbRespeaker(Context context) {
		recorder = new Recorder(context);
		player = new Player();
		mapper = new Mapper();
		setFinishedPlaying(false);
		playThroughSpeaker();
	}
	*/

	public void playOriginal() {
		player.seekToSample(mapper.getOriginalStartSample());
		mapper.markOriginal(player);
		player.play();
	}

	public void pauseOriginal() {
		player.pause();
	}

	public void recordRespeaking() {
		mapper.markRespeaking(player, recorder);
		recorder.listen();
	}

	public void pauseRespeaking() throws MicException {
		recorder.pause();
		mapper.store(player, recorder);
	}

	public void stop() throws MicException {
		recorder.stop();
		player.pause();
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

	public SimplePlayer getSimplePlayer() {
		return this.player;
	}

	/** Player to play the original with. */
	private SimplePlayer player;

	/** The recorder used to get respeaking data. */
	private Recorder recorder;
	
	/** The mapper used to store mapping data. */
	private Mapper mapper;

	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying;
}
