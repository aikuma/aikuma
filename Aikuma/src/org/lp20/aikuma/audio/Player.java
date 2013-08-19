package org.lp20.aikuma.audio;

public abstract class Player {

	/** The class that is to be implemented that is given to a Player and has
	 * it's onCompletion method run when the player completes playing a
	 * recording. */
	public static abstract class OnCompletionListener {
		public abstract void onCompletion(Player player);
	}

	/** Set the callback to be run when the recording completes playing. */
	public abstract void setOnCompletionListener(OnCompletionListener listener);

	/** Get the duration of the recording in milliseconds. */
	public abstract int getDurationMsec();

	/** Seek to a given point in the recording in milliseconds. */
	public abstract void seekToMsec(int msec);

	/** Indicates whether the recording is currently being played. */
	public abstract boolean isPlaying();

	/** Pauses the playback. */
	public abstract void pause();

	/** Starts or resumes playback of the recording. */
	public abstract void play();

	/** Get current point in the recording in milliseconds. */
	public abstract int getCurrentMsec();

	/** Releases resources associated with the player */
	public abstract void release();

	public abstract int sampleToMsec(long sample);

}
