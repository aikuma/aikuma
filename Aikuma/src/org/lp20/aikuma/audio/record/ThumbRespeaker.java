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
import java.io.IOException;
import java.util.UUID;

import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.record.Microphone.MicException;
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

	/**
	 * Constructor
	 *
	 * @param	original	The original recording to make a respeaking of.
	 * @param	respeakingUUID	The UUID of the respeaking we will create.
	 * @param	rewindAmount	Rewind-amount in msec after each respeaking-segment
	 * @throws	MicException	If the microphone couldn't be used.
	 * @throws	IOException	If there is an I/O issue.
	 */
	public ThumbRespeaker(Recording original, UUID respeakingUUID, 
			int rewindAmount) throws MicException, IOException {
		recorder = new Recorder(new File(Recording.getNoSyncRecordingsPath(),
				respeakingUUID + ".wav"), original.getSampleRate());
		player = new SimplePlayer(original, true);
		mapper = new Mapper(respeakingUUID);
		setFinishedPlaying(false);
		this.rewindAmount = rewindAmount;
	}

	/**
	 * Plays the original recording.
	 */
	/**
	 * Plays the original recording.
	 * @param playMode	(0:Continue, 1:Rewind and Store, 2:Only rewind)
	 */
	public void playOriginal(int playMode) {
		switch(playMode) {
		case 0: // Continue playing
			break;
		case 1:	//Rewind and record the start-sample in mapper
			player.seekToSample(mapper.getOriginalStartSample());
			mapper.markOriginal(player);
			player.rewind(rewindAmount);
			break;
		case 2:	//Only rewind to the previous point
			player.seekToSample(previousEndSample);
			break;
		}
		previousEndSample = player.getCurrentSample();
		player.play();
	}

	/**
	 * Pauses playing of the original recording.
	 */
	public void pauseOriginal() {
		player.pause();		
	}

	/**
	 * Activates recording of the respeaking.
	 */
	public void recordRespeaking() {
		mapper.markRespeaking(player, recorder);
		recorder.listen();
	}

	/**
	 * Pauses the respeaking process.
	 *
	 * @throws	MicException	If the micrphone recording couldn't be paused.
	 */
	public void pauseRespeaking() throws MicException {
		recorder.pause();
		// Because of rewind after each respeaking-segment,
		// Force user to record respeaking after listening next original-segment
		if(player.getCurrentSample() > mapper.getOriginalStartSample()) {
			mapper.store(player, recorder);
		}
	}

	/**
	 * Stops/finishes the respeaking process
	 *
	 * @throws	MicException	If there is an issue stopping the microphone.
	 * @throws	IOException	If the mapping between original and respeaking
	 * couldn't be written to file.
	 */
	public void stop() throws MicException, IOException {
		recorder.stop();
		player.pause();
		mapper.stop();
	}

	public void setFinishedPlaying(boolean finishedPlaying) {
		this.finishedPlaying = finishedPlaying;
	}

	public int getCurrentMsec() {
		return recorder.getCurrentMsec();
	}

	/**
	 * finishedPlaying accessor
	 *
	 * @return	true if the original recording has finished playing; false
	 * otherwise.
	 */
	public boolean getFinishedPlaying() {
		return this.finishedPlaying;
	}

	/**
	 * Sets the callback to be run when the original recording has finished
	 * playing.
	 *
	 * @param	ocl	The callback to be played on completion.
	 */
	public void setOnCompletionListener(OnCompletionListener ocl) {
		player.setOnCompletionListener(ocl);
	}

	public SimplePlayer getSimplePlayer() {
		return this.player;
	}

	/**
	 * Releases the resources associated with this respeaker.
	 */
	public void release() {
		if (player != null) {
			player.release();
		}
	}

	public Recorder getRecorder() {
		return this.recorder;
	}

	/** Player to play the original with. */
	private SimplePlayer player;

	/** The recorder used to get respeaking data. */
	private Recorder recorder;
	
	/** The mapper used to store mapping data. */
	private Mapper mapper;

	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying;
	
	/** Previous sample-point */
	private long previousEndSample;

	
	/** The amount to rewind the original in msec 
	 * after each respeaking-segment. */
	private int rewindAmount;
}
