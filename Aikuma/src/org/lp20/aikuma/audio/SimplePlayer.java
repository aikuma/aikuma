package org.lp20.aikuma.audio;

import android.util.Log;
import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SimplePlayer extends Player implements Sampler {

	/**
	 * Creates a player to play the supplied recording.
	 *
	 * @param	recording	The metadata of the recording to play.
	 */
	public SimplePlayer(Recording recording) throws IOException {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(recording.getFile().getCanonicalPath());
		mediaPlayer.prepare();
		setSampleRate(recording.getSampleRate());
	}

	/**
	 * Creates a player to play the supplied recording for when no Recording
	 * metadata file exists
	 *
	 * @param	file	The location of the recording as a File
	 * @param	sampleRate	The sample rate of the recording
	 */
	public SimplePlayer(File recordingFile, int sampleRate) throws IOException {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(recordingFile.getCanonicalPath());
		mediaPlayer.prepare();
		setSampleRate(sampleRate);
	}

	/** Starts or resumes playback of the recording. */
	public void play() {
		mediaPlayer.start();
	}

	/** Pauses the playback. */
	public void pause() {
		mediaPlayer.pause();
	}

	/** Indicates whether the recording is currently being played. */
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	/** Get current point in the recording in milliseconds. */
	public int getCurrentMsec() {
		return mediaPlayer.getCurrentPosition();
	}

	public long getCurrentSample() {
		return msecToSample(getCurrentMsec());
	}

	/** Get the duration of the recording in milliseconds. */
	public int getDurationMsec() {
		return mediaPlayer.getDuration();
	}

	/** Seek to a given point in the recording in milliseconds. */
	public void seekToMsec(int msec) {
		mediaPlayer.seekTo(msec);
	}

	public void seekToSample(long sample) {
		seekToMsec(sampleToMsec(sample));
	}

	/** Releases the resources associated with the SimplePlayer */
	public void release() {
		mediaPlayer.release();
	}

	/** Set the callback to be run when the recording completes playing. */
	public void setOnCompletionListener(final OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(
				new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer _mp) {
						listener.onCompletion(SimplePlayer.this);
					}
				});
	}

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

	public int sampleToMsec(long sample) {
		long msec = sample / (getSampleRate() / 1000);
		if (msec > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) msec;
	}

	public long msecToSample(int msec) {
		return msec * (getSampleRate() / 1000);
	}
	
	public void setAudioStreamType(int type) {
		mediaPlayer.setAudioStreamType(type);
	}

	/** The MediaPlayer used to play the recording. **/
	private MediaPlayer mediaPlayer;
	private long sampleRate;
}
