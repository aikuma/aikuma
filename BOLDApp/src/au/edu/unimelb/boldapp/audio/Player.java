package au.edu.unimelb.aikuma.audio;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;

/**
 * An extension of MediaPlayer
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Player extends MediaPlayer{

	/**
	 * returns the sample rate of the file being played
	 */
	public int getSampleRate() {
		return Constants.SAMPLE_RATE;
	}

	/**
	 * Returns the current sample
	 */
	public long getCurrentSample() {
		int milliseconds = getCurrentPosition();
		double sample = milliseconds * (getSampleRate() / (float) 1000);
		return (long) sample;
	}

	/**
	 * Indicates whether the audio is currently being played
	 */
	private boolean playing = false;

	/**
	 * playing accessor
	 *
	 * @return	value of playing.
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * playing mutator
	 *
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/**
	 * Prepare playing with the given file.
	 *
	 * @param	fileName	The name of the file to be played.
	 */
	public void prepare(String fileName) {
		try {
			setDataSource(generateFilePath(fileName));
			prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start playing.
	 */
	public void play() {
		if (playing) {
			return;
		}
		playing = true;
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Rewind the player a number of miliseconds. */
	public void rewind(int miliseconds) {
		int currentPosition = getCurrentPosition();
		int targetPosition = currentPosition - miliseconds;
		if (targetPosition < 0) {
			targetPosition = 0;
		}
		seekTo(targetPosition);
	}

	/** Resume the player. */
	public void resume() {
		start();
		playing = true;
	}

	/** Pause */
	public void pause() {
		super.pause();
		playing = false;
	}

	/** Stop the player. */
	public void stop() {
		if (!playing) {
			return;
		}
		reset();
		playing = false;
	}

	/** Generates all the necessary directories for the file. */
	protected String generateFilePath(String fileName) {
		File file = new File(fileName);
		File parentFile = new File(file.getParent());
		parentFile.mkdirs();
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
