package au.edu.unimelb.boldapp.audio;

import android.media.MediaPlayer;
import java.util.UUID;

/**
 * Offers functionality to play a respeaking interleaved with the original.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class InterleavedPlayer implements PlayerInterface {
	/**
	 * The object that represents the mapping between segments of the original
	 * and the respeaking.
	 */
	private Segments segments;

	/**
	 * Standard Constructor; takes the UUID of the respeaking.
	 *
	 * @param	respeakingUUID	The UUID of the respeaking.
	 */
	public InterleavedPlayer(UUID respeakingUUID) throws Exception {
		this.segments = new Segments(respeakingUUID);
	}

	/**
	 * Gets the current playback position.
	 *
	 * @return the current position in milliseconds.
	 */
	public int getCurrentPosition() {
		return 1234;
	}

	/**
	 * Checks whether the MediaPlayer is playing.
	 *
	 * @return	true if currently playing; false otherwise.
	 */
	public boolean isPlaying() {
		return false;
	}

	/**
	 * Starts and resumes playback; if playback had previously been paused,
	 * playback will resume from where it was paused; if playback had been
	 * stopped, or never started before, playback will start at the beginning.
	 */
	public void start() {
	}

	/**
	 * Register a callback to be invoked when the end of a media source has
	 * been reached during playback.
	 *
	 * @param	listener	the callback that will be run.
	 */
	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener listener) {
	}

	/**
	 * Releases resources associated with this Player.
	 */
	public void release() {
	}

	/**
	 * Pauses playback; call start() to resume.
	 */
	public void pause() {
	}

	/**
	 * Gets the duration of the file.
	 *
	 * @return	the duration of the file in milliseconds.
	 */
	public int getDuration() {
		return 1234;
	}

	/**
	 * Seeks to the specified time position.
	 *
	 * @param	msec	the offset in milliseconds from the start to seek to.
	 */
	public void seekTo(int msec) {
	}

	/**
	 * Rewinds the player a number of milliseconds.
	 *
	 * @param	msec	The amount of milliseconds to rewind.
	 */
	public void rewind(int msec) {
	}
}
