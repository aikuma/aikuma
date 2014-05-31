/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

import android.media.AudioManager;
import android.content.Context;

import android.util.Log;
import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.util.FileIO;

/**
 * A wrapper class for android.media.MediaPlayer that makes simpler the task of
 * playing an Aikuma recording.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SimplePlayer extends Player implements Sampler {

	/**
	 * Creates a player to play the supplied recording.
	 *
	 * @param	recording	The metadata of the recording to play.
	 * @param	playThroughSpeaker	True if the audio is to be played through the main
	 * speaker; false if through the ear piece (ie the private phone call style)
	 * @throws	IOException	If there is an issue reading the audio source.
	 */
	public SimplePlayer(Recording recording, boolean playThroughSpeaker)
			throws IOException {
		setRecording(recording);
		mediaPlayer = new MediaPlayer();
		if (playThroughSpeaker) {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} else {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		mediaPlayer.setDataSource(recording.getFile().getCanonicalPath());
		mediaPlayer.prepare();
		setSampleRate(recording.getSampleRate());
	}

	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	/**
	 * Creates a player to play the supplied recording for when no Recording
	 * metadata file exists
	 *
	 * @param	recordingFile	The location of the recording as a File
	 * @param	sampleRate	The sample rate of the recording
	 * @param	playThroughSpeaker	True if the audio is to be played through the main
	 * speaker; false if through the ear piece (ie the private phone call style)
	 * @throws	IOException	If there is an issue reading the audio source.
	 */
	public SimplePlayer(File recordingFile, long sampleRate,
			boolean playThroughSpeaker) throws IOException {
		mediaPlayer = new MediaPlayer();
		if (playThroughSpeaker) {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} else {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		mediaPlayer.setDataSource(recordingFile.getCanonicalPath());
		mediaPlayer.prepare();
		setSampleRate(sampleRate);
	}

	/** Starts or resumes playback of the recording. */
	public void play() {
		this.finishedPlaying = false;
		mediaPlayer.start();
	}

	/** Pauses the playback. */
	public void pause() {
		try {
			// If it's not in a started state, then the OnCompletionListener
			// may be called when pause() is, so ensure it's already playing
			// before calling pause().
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		} catch (IllegalStateException e) {
			//If it's in an illegal state, then it wouldn't be playing anyway,
			//so no issue.
		}
	}

	/**
	 * Indicates whether the recording is currently being played.
	 *
	 * @return	true if the player is currently playing; false otherwise.
	 */
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	/**
	 * Get current point in the recording in milliseconds.
	 *
	 * @return	The current point in the recording in milliseconds as an int.
	 */
	public int getCurrentMsec() {
		try {
			return mediaPlayer.getCurrentPosition();
		} catch (IllegalStateException e) {
			//If we get an IllegalStateException because the recording has
			//finished playing, just return the duration of the recording.
			return getDurationMsec();
		}
	}

	public long getCurrentSample() {
		return msecToSample(getCurrentMsec());
	}

	/**
	 * Get the duration of the recording in milliseconds.
	 *
	 * @return	The duration of the audio in milliseconds as an int.
	 */
	public int getDurationMsec() {
		try {
			return mediaPlayer.getDuration();
		} catch (IllegalStateException e) {
			//If this fails then we won't be in an activity where a duration is
			//actually required. -1 is returned so we at least know it was
			//erroneous if we ever need to.
			return -1;
		}
	}

	/**
	 * Seek to a given point in the recording in milliseconds.
	 *
	 * @param	msec	The time to jump the playback to in milliseconds.
	 */
	public void seekToMsec(int msec) {
		mediaPlayer.seekTo(msec);
	}

	/**
	 * Moves the recording to the given sample.
	 *
	 * @param	sample	The sample to jump playback to.
	 */
	public void seekToSample(long sample) {
		seekToMsec(sampleToMsec(sample));
	}

	/** Releases the resources associated with the SimplePlayer */
	public void release() {
		mediaPlayer.release();
	}

	/**
	 * Set the callback to be run when the recording completes playing.
	 *
	 * @param	listener	The callback to be called when the recording
	 * complets playing.
	 */
	public void setOnCompletionListener(final OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(
				new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer _mp) {
						listener.onCompletion(SimplePlayer.this);
						try {
							incrementViewCount();
						} catch (IOException e) {
							// There is likely an issue writing to the
							// filesystem, but it's probably not worth
							// reporting to the user with at Toast.
						}
						SimplePlayer.this.finishedPlaying = true;
					}
				});
	}

	/**
	 * Increment the view count by adding a view file to the Recording's view
	 * directory.
	 */
	private void incrementViewCount() throws IOException {
		File viewDir = new File(FileIO.getAppRootPath(), "views/" +
				recording.getGroupId() + "/" + recording.getId());
		viewDir.mkdirs();

		File viewFile = new File(viewDir, UUID.randomUUID() + ".view");
		viewFile.createNewFile();
	}

	/**
	 * Returns the sample rate of this recording
	 *
	 * @return	The sample rate of this recording.
	 */
	public long getSampleRate() {
		//If the sample rate is less than zero, then this indicates that there
		//wasn't a sample rate found in the metadata file.
		if (sampleRate <= 0l) {
			throw new RuntimeException(
					"The sampleRate of the recording is not known.");
		}
		return sampleRate;
	}

	private void setSampleRate(long sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * Converts a value of samples into milliseconds assuming this recording's
	 * sample rate.
	 *
	 * @param	sample	sample value to be converted.
	 * @return	A millisecond value as an integer.
	 */
	public int sampleToMsec(long sample) {
		long msec = sample / (getSampleRate() / 1000);
		if (msec > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) msec;
	}

	/**
	 * Converts a millisecond value into samples assuming this recording's
	 * sample rate.
	 *
	 * @param	msec	A time value in milliseconds.
	 * @return	A sample value as a long.
	 */
	public long msecToSample(int msec) {
		return msec * (getSampleRate() / 1000);
	}

	public boolean isFinishedPlaying() {
		return this.finishedPlaying;
	}

	/** The MediaPlayer used to play the recording. **/
	private MediaPlayer mediaPlayer;
	private long sampleRate;
	private boolean finishedPlaying;
	private Recording recording;
}
