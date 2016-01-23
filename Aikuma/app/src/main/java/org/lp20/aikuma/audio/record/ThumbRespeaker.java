/*
	Copyright (C) 2013-2015, The Aikuma Project
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
import java.util.Iterator;
import java.util.UUID;

import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.audio.MarkedPlayer;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.Sampler;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.MarkedPlayer.OnMarkerReachedListener;
import org.lp20.aikuma.audio.Player.OnCompletionListener;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;

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
	 * @param 	type			Output-type (0: Respeak/Interpret, 1:Segment)
	 * @throws	MicException	If the microphone couldn't be used.
	 * @throws	IOException	If there is an I/O issue.
	 */
	public ThumbRespeaker(Recording original, UUID respeakingUUID, 
			int rewindAmount, int type) throws MicException, IOException {
		// segment on segment		: Disable play-button when marker is reached. Play source
		// segment on derivative(=derivative's segment)	: Disable play-button when marker is reached. Play source
		// derivative on segment	: Disable record-button until marker is reached. Play source
		// derivative on derivative	: Disable record-button until marker is reached. Play derivative, conversion segments to source-segments(1-to-1)
				
		outputType = type;
		isRecordingOriginal = original.isOriginal();
		isRecordingSegment = original.getFileType().equals(Recording.SEGMENT_TYPE);
		
		// Initialize Recorder
		if(type == 1) {	// In case of segmentation activity
			recorder = null;
		}
		else {			// In case of the creation of derivative recording
			if(isRecordingOriginal) {
				recorder = new Recorder(1, new File(Recording.getNoSyncRecordingsPath(),
						respeakingUUID + ".wav"), original.getSampleRate());
			} else {
				recorder = new Recorder(1, new File(Recording.getNoSyncRecordingsPath(),
						respeakingUUID + ".wav"), original.getOriginal().getSampleRate());
			}
		}
		
		// Initialize Player
		if(isRecordingOriginal)
			player = new SimplePlayer(original, true);
		else {
			segments = new Segments(original);
			segmentIterator = segments.getOriginalSegmentIterator();
			advanceSegment();
			Log.i("segments", segments.toString());
			
			if(type == 1 || isRecordingSegment) {
				player = new MarkedPlayer(original.getOriginal(), true);
				isMappingToOriginal = false;
			}
			else {
				player = new MarkedPlayer(original, true);
				isMappingToOriginal = true;	// conversion of mapping/segment to source is necessary
			}
		}
			
		mapper = new Mapper(respeakingUUID);
		setFinishedPlaying(false);
		this.rewindAmount = rewindAmount;
	}

	/**
	 * Plays the original recording.
	 * @param playMode	(0:Continue, 1:Rewind and Store, 2:Only rewind)
	 */
	public void playOriginal(int playMode) {
		// set notification to current segment
		if(!isRecordingOriginal) {
			if(isMappingToOriginal) { // conversion of mapping/segment to source
				((MarkedPlayer)player).setNotificationMarkerPosition(currentRespeakSegment);
			} else {
				((MarkedPlayer)player).setNotificationMarkerPosition(currentOriginalSegment);
			}
		}	
		
		switch(playMode) {
		case 0: // Continue playing
			break;
		case 1:	//Rewind and record the start-sample in mapper
			player.seekToSample(mapper.getOriginalStartSample());
			if(isMappingToOriginal) {
				Log.i("segment", ""+currentOriginalSegment.getStartSample());
				mapper.markOriginal(new Sampler() {
					@Override
					public long getCurrentSample() {
						return currentOriginalSegment.getStartSample();
					}
				});
			} else {
				mapper.markOriginal(player);
			}
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
		if(isMappingToOriginal) {	// When creating respeak/interpret on respeak/interpret
			Log.i("segment", ""+currentOriginalSegment.getEndSample());
			mapper.markRespeaking(new Sampler() {
				@Override
				public long getCurrentSample() {
					return currentOriginalSegment.getEndSample();
				}
			}, recorder);
		}
		else {
			mapper.markRespeaking(player, recorder);
		}
		
		if(recorder != null)
			recorder.listen();
		
		// move notification to next segment
		if(isRecordingSegment) {
			if(Math.abs(player.sampleToMsec(	// 100msec(marker notification period) + 50msec(error range)
					currentOriginalSegment.getEndSample() - player.getCurrentSample())) <= 150) {
				Log.i("seg", ""+ player.sampleToMsec(
					currentOriginalSegment.getEndSample() - player.getCurrentSample()));
				advanceSegment();
			}
		}
		else if(!isRecordingOriginal) {
			advanceSegment();
		}	
	}

	/**
	 * Pauses the respeaking process.
	 *
	 * @throws	MicException	If the micrphone recording couldn't be paused.
	 */
	public void pauseRespeaking() throws MicException {
		if(recorder != null)
			recorder.pause();
	}

	/**
	 * Saves the respeaking audio and mapping-information
	 */
	public void saveRespeaking() {
		if(recorder != null)
			recorder.save();
		
		// Because of rewind after each respeaking-segment,
		// Force user to record respeaking after listening next original-segment
		if(player.getCurrentSample() > mapper.getOriginalStartSample()) {
			if(isMappingToOriginal) {		// When creating respeak/interpret on respeak/interpret
				mapper.store(new Sampler() {
					@Override
					public long getCurrentSample() {
						return currentOriginalSegment.getStartSample();
					}
				}, recorder);
			}
			else {
				mapper.store(player, recorder);
			}
			
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
		if(recorder != null)
			recorder.stop();
		player.pause();
		mapper.stop();
	}

	public void setFinishedPlaying(boolean finishedPlaying) {
		this.finishedPlaying = finishedPlaying;
	}
	
	/**
	 * Returns the recorder's current position in msec
	 * @return	the current pos of the recorder
	 */
	public int getCurrentMsec() {
		if(recorder == null)
			return 0;
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
	
	/**
	 * Sets the callback to be run when the original recording has reached
	 * any marker
	 *
	 * @param	oml	The callback to be played on reaching markers
	 */
	public void setOnMarkerReachedListener(final OnMarkerReachedListener oml) {
		if(!isRecordingOriginal) {
			((MarkedPlayer)player).setOnMarkerReachedListener(oml);
		}
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
	
	/**
	 * Returns the output type of this thumb-activity
	 * @return	Output-type (0: Respeak/Interpret, 1:Segment)
	 */
	public int getOutputType() {
		return this.outputType;
	}
	
	/**
	 * Returns if the recording is original
	 * @return	true if the recording is original, else false
	 */
	public boolean isRecordingOrignal() {
		return this.isRecordingOriginal;
	}
	
	// Moves forward to the next segment in the recording.
	// In case of derivative on derivative, source and derivative's segments
	// need to be moved together for the conversion of mapping to source
	private void advanceSegment() {
		if (segmentIterator.hasNext()) {
			currentOriginalSegment = segmentIterator.next();
			currentRespeakSegment = segments.getRespeakingSegment(currentOriginalSegment);
		} else {
			long startSample, startSample2;
			if (currentOriginalSegment != null) {
				startSample = currentOriginalSegment.getEndSample();
			} else {
				startSample = 0l;
			}
			if (currentRespeakSegment != null) {
				startSample2 = currentRespeakSegment.getEndSample();
			} else {
				startSample2 = 0l;
			}
			currentOriginalSegment = new Segment(startSample, Long.MAX_VALUE);
			currentRespeakSegment = new Segment(startSample2, Long.MAX_VALUE);
		}
		//Log.i("release", "start: " + currentOriginalSegment.getStartSample() + ": " + currentOriginalSegment.getEndSample());
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
	
	/** Indicates if the recording is 
	 * original or others(segment/respeak/interpret). */
	private boolean isRecordingOriginal;
	private boolean isRecordingSegment;
	private boolean isMappingToOriginal;
	
	private int outputType;
	
	private Segments segments;
	private Iterator<Segment> segmentIterator;
	private Segment currentOriginalSegment;
	private Segment currentRespeakSegment;
}
