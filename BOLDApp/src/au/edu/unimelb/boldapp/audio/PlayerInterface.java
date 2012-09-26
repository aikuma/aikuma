package au.edu.unimelb.boldapp.audio;

import android.media.MediaPlayer;

/**
 * The interface for all audio players in the package.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface PlayerInterface {
	/**
	 * Returns the sample rate of the file being played, in Hz.
	 *
	 * @return	The sample rate of the file being played.
	 */
	int getSampleRate();

	/**
	 * Returns the current sample; a pointer to the current location in the
	 * audio file.
	 *
	 * @return	The current sample.
	 */
	long getCurrentSample();

	/**
	 * Checks whether the MediaPlayer is playing
	 *
	 * @return	true if currently playing, false otherwise
	 */
	boolean isPlaying();

	void start();
	void setOnCompletionListener(MediaPlayer.OnCompletionListener listener);
	void release();
	void pause();
	int getCurrentPosition();
	int getDuration();
	void seekTo(int msec);


}
