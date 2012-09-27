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
	 * Gets the current playback position.
	 *
	 * @return	the current position in milliseconds
	 */
	int getCurrentPosition();

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
	/**
	 * Releases resources associated with this Player.
	 */
	void release();
	void pause();
	int getDuration();
	/**
	 * Seeks to the specified time position
	 *
	 * @param	msec	the offset in milliseconds from the start to seek to.
	 */
	void seekTo(int msec);


}
