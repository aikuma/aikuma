package au.edu.unimelb.aikuma.audio;

import android.media.MediaPlayer;
import android.util.Log;
import au.edu.unimelb.aikuma.FileIO;
import au.edu.unimelb.aikuma.Recording;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
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

	private Segment currentOriginalSegment;

	/**
	 * Counter which indicates which segment in the original and respeaking the
	 * player is up to; is incremented after both the original and respeaking
	 * have been played for a given segment.
	 */
	private int segmentCount;

	private void playSegment(
			Segment segment, SimplePlayer player) {
		if (segment != null) {
			if (player == original) {
				Log.i("segments2", "playing segment on original: " + segment);
			} else if (player == respeaking) {
				Log.i("segments2", "playing segment on respeaking: " + segment);
			}
			player.seekTo(segment);
			player.setNotificationMarkerPosition(segment);
			player.start();
		} else {
			Log.i("segments2", " " + segment);
		}
	}

	/**
	 * Standard Constructor; takes the UUID of the respeaking.
	 *
	 * @param	respeakingUUID	The UUID of the respeaking.
	 */
	public InterleavedPlayer(UUID respeakingUUID) throws Exception {
		initializePlayers(respeakingUUID);
		this.segments = new NewSegments(respeakingUUID);
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

	public Iterator<Segment> getOriginalSegmentIterator() {
		if (originalSegmentIterator == null) {
			originalSegmentIterator = segments.getOriginalSegmentIterator();
		}
		return originalSegmentIterator;
	}

	private Segment getCurrentOriginalSegment() {
		if (currentOriginalSegment == null) {
			advanceSegment();
		}
		return currentOriginalSegment;
	}

	// This should only ever be called from the respeaking notification marker
	// listener
	private void advanceSegment() {
		if (getOriginalSegmentIterator().hasNext()) {
			currentOriginalSegment = getOriginalSegmentIterator().next();
		} else {
			long startSample;
			if (currentOriginalSegment != null) {
				startSample = currentOriginalSegment.getEndSample();
			} else {
				startSample = 0l;
			}
			currentOriginalSegment = new Segment(startSample, Long.MAX_VALUE);
		}
	}

	/**
	 * Starts and resumes playback; if playback had previously been paused,
	 * playback will resume from where it was paused; if playback had been
	 * stopped, or never started before, playback will start at the beginning.
	 */
	public void start() {
		original.mediaPlayer.setOnErrorListener(new
		MediaPlayer.OnErrorListener() {
			public void onError(MediaPlayer _mp) {
				// TODO
			}
		}
		playOriginal();
		// playSegment(getCurrentOriginalSegment(), original);
		/*
		if (toPlayOriginal) {
			original.start();
		} else {
			respeaking.start();
		}
		*/
	}

	/**
	 * Register a callback to be invoked when the end of a media source has
	 * been reached during playback.
	 *
	 * @param	listener	the callback that will be run.
	 */
	public void setOnCompletionListener(
			final MediaPlayer.OnCompletionListener listener) {
		MediaPlayer.OnCompletionListener bothCompletedListener = new
		MediaPlayer.OnCompletionListener() {
			boolean completedOnce = false;
			@Override
			public void onCompletion(MediaPlayer _mp) {
				Log.i("segments2", "completedonce: " + completedOnce);
				if (_mp == original.mediaPlayer) {
					Log.i("segments2", "mp in bothCompletedListener: original");
				} else if (_mp == respeaking.mediaPlayer) {
					Log.i("segments2", "mp in bothCompletedListener: respeaking");
				} else {
					Log.i("segments2", " " + _mp);
				}
				if (completedOnce) {
					listener.onCompletion(_mp);
					currentOriginalSegment = null;
					originalSegmentIterator = segments.getOriginalSegmentIterator();
					completedOnce = false;
				} else {
					completedOnce = true;
				}
			}
		};
		original.setOnCompletionListener(bothCompletedListener);
		respeaking.setOnCompletionListener(bothCompletedListener);
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
		Log.i("segments", "pausing some stuff");
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
			playRespeaking();
		}
	}

	private void playRespeaking() {
		playSegment(segments.getRespeakingSegment(getCurrentOriginalSegment()), respeaking);
	}

	private class RespeakingMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedMediaPlayer p) {
			respeaking.pause();
			advanceSegment();
			playOriginal();
		}
	}

	private void playOriginal() {
		playSegment(getCurrentOriginalSegment(), original);
	}
}
