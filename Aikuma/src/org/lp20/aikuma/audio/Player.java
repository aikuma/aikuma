/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

/**
 * A player interface that is to be implemented by all classes that play audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class Player {

	/**
	 * The class that is to be implemented that is given to a Player and has
	 * it's onCompletion method run when the player completes playing a
	 * recording.
	 */
	public static abstract class OnCompletionListener {

		/**
		 * The method called when the player gets to completion.
		 *
		 * @param	player	The player
		 */
		public abstract void onCompletion(Player player);
	}

	/**
	 * Set the callback to be run when the recording completes playing.
	 *
	 * @param	listener	The callback to be invoked
	 */
	public abstract void setOnCompletionListener(OnCompletionListener listener);

	/**
	 * Get the duration of the recording in milliseconds.
	 *
	 * @return	The duration of the recording in milliseconds.
	 **/
	public abstract int getDurationMsec();

	/**
	 * Seek to a given point in the recording in milliseconds.
	 *
	 * @param	msec	The point in the recording to seek to (milliseconds).
	 */
	public abstract void seekToMsec(int msec);

	/**
	 * Indicates whether the recording is currently being played.
	 *
	 * @return	true if recording is being played; false otherwise.
	 */
	public abstract boolean isPlaying();

	/** Pauses the playback. */
	public abstract void pause();

	/** Starts or resumes playback of the recording. */
	public abstract void play();

	/**
	 * Get current point in the recording in milliseconds.
	 *
	 * @return	The current position in milliseconds as an integer.
	 */
	public abstract int getCurrentMsec();

	/**
	 * Releases resources associated with the player
	 */
	public abstract void release();

	/**
	 * Converts a sample to milliseconds.
	 *
	 * @param	sample	The sample.
	 * @return	The milliseconds as an integer.
	 */
	public abstract int sampleToMsec(long sample);

}
