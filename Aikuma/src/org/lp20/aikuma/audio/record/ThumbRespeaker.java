/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
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

/**
 * Facilitates respeaking of an original recording by offering methods to start
 * and pause playing the original, and start and pause recording the
 * respeaking.
 * 
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeaker {

	public ThumbRespeaker(Recording original, UUID respeakingUUID)
			throws MicException, IOException {
		recorder = new Recorder(new File(Recording.getNoSyncRecordingsPath(),
				respeakingUUID + ".wav"), original.getSampleRate());
		player = new SimplePlayer(original, true);
		mapper = new Mapper(respeakingUUID);
		setFinishedPlaying(false);
	}

	/*
	public ThumbRespeaker(Context context) {
		recorder = new Recorder(context);
		player = new Player();
		mapper = new Mapper();
		setFinishedPlaying(false);
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

	/** finishedPlaying mutator */
	 public void setFinishedPlaying(boolean finishedPlaying) {
	 	this.finishedPlaying = finishedPlaying;
	 }

	public int getCurrentMsec() {
		return recorder.getCurrentMsec();
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

	public void release() {
		if (player != null) {
			player.release();
		}
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
