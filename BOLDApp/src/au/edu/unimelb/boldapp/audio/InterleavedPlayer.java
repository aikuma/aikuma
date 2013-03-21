package au.edu.unimelb.aikuma.audio;

import android.media.MediaPlayer;
import android.util.Log;
import au.edu.unimelb.aikuma.FileIO;
import au.edu.unimelb.aikuma.Recording;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import au.edu.unimelb.aikuma.audio.NewSegments.Segment;

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
	private NewSegments segments;

	/**
	 * The Player for the original audio.
	 */
	private SimplePlayer original;

	/**
	 * The Player for the respeaking audio.
	 */
	private SimplePlayer respeaking;

	/**
	 * Boolean that indicates whether to play the original or not; used when
	 * the InterleavedPlayer is to start playing after being paused to
	 * determine which player to use.
	 */
	private boolean toPlayOriginal;

	private Iterator<Segment> originalSegmentIterator;

	private Segment currentSegment;

	/**
	 * Counter which indicates which segment in the original and respeaking the
	 * player is up to; is incremented after both the original and respeaking
	 * have been played for a given segment.
	 */
	private int segmentCount;

	private void playSegment(
			Segment segment, SimplePlayer player) {
		player.seekTo(respeaking.sampleToMsec(segment.getStartSample()));
		player.setNotificationMarkerPosition(
				player.sampleToMsec(segment.getEndSample()));
	}

	/**
	 * Standard Constructor; takes the UUID of the respeaking.
	 *
	 * @param	respeakingUUID	The UUID of the respeaking.
	 */
	public InterleavedPlayer(UUID respeakingUUID) throws Exception {
		this.initializePlayers(respeakingUUID);
		this.segments = new NewSegments(respeakingUUID);
		Log.i("segments", respeakingUUID.toString());
		Log.i("segments", segments.segmentMap.keySet().toString());
		this.originalSegmentIterator = segments.getOriginalSegmentIterator();
		toPlayOriginal = true;
	}

	private void initializePlayers(UUID respeakingUUID) throws IOException {
		Recording respeakingMeta = FileIO.readRecording(respeakingUUID);
		UUID originalUUID = respeakingMeta.getOriginalUUID();
		original = new SimplePlayer(originalUUID, new
				OriginalMarkerReachedListener());
		respeaking = new SimplePlayer(respeakingUUID, new
				RespeakingMarkerReachedListener());
	}

	/**
	 * Gets the current playback position.
	 *
	 * @return the current position in milliseconds.
	 */
	public int getCurrentPosition() {
		return original.getCurrentPosition() + respeaking.getCurrentPosition();
	}

	/**
	 * Checks whether the MediaPlayer is playing.
	 *
	 * @return	true if currently playing; false otherwise.
	 */
	public boolean isPlaying() {
		return original.isPlaying() || respeaking.isPlaying();
	}

	/**
	 * Starts and resumes playback; if playback had previously been paused,
	 * playback will resume from where it was paused; if playback had been
	 * stopped, or never started before, playback will start at the beginning.
	 */
	public void start() {
		if (toPlayOriginal) {
			Log.i("InterleavedPlayer", "starting original");
			original.start();
		} else {
			respeaking.start();
		}
	}

	/**
	 * Register a callback to be invoked when the end of a media source has
	 * been reached during playback.
	 *
	 * @param	listener	the callback that will be run.
	 */
	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener listener) {
		original.setOnCompletionListener(listener);
		respeaking.setOnCompletionListener(listener);
	}

	/**
	 * Releases resources associated with this Player.
	 */
	public void release() {
		original.release();
		respeaking.release();
	}

	/**
	 * Pauses playback; call start() to resume.
	 */
	public void pause() {
		original.pause();
		respeaking.pause();
	}

	/**
	 * Gets the duration of the file.
	 *
	 * @return	the duration of the file in milliseconds.
	 */
	public int getDuration() {
		return original.getDuration() + respeaking.getDuration();
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

	private class OriginalMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedMediaPlayer p) {
			original.pause();
			playSegment(segments.getRespeakingSegment(currentSegment), respeaking);
		}
	}

	private class RespeakingMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedMediaPlayer p) {
			respeaking.pause();
			currentSegment = originalSegmentIterator.next();
			playSegment(currentSegment, original);
		}
	}
}
