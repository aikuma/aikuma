package org.lp20.aikuma.audio;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;
import java.util.Iterator;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class InterleavedPlayer extends Player {

	/**
	 * Creates an InterleavedPlayer to play the supplied recording and it's
	 * original
	 *
	 * @param	recording	The metadata of the recording to play.
	 */
	public InterleavedPlayer(Recording recording) throws Exception {
		if (recording.isOriginal()) {
			throw new IllegalArgumentException("The supplied Recording is " +
					"not a respeaking. Use SimplePlayer instead.");
		}
		original = new MarkedPlayer(
				Recording.read(recording.getOriginalUUID()),
				new OriginalMarkerReachedListener());
		respeaking = new MarkedPlayer(recording,
				new RespeakingMarkerReachedListener());
		segments = new Segments(recording.getUUID());
	}

	public void play() {
		playOriginal();
	}

	/** Indicates whether the recording is currently being played. */
	public boolean isPlaying() {
		return original.isPlaying() || respeaking.isPlaying();
	}

	/** Pauses the playback. */
	public void pause() {
		if (original.isPlaying()) {
			original.pause();
		} else if (respeaking.isPlaying()) {
			respeaking.pause();
		}
	}

	/** Get current point in the recording in milliseconds. */
	public int getCurrentPositionMsec() {
		return original.getCurrentPositionMsec() +
				respeaking.getCurrentPositionMsec();
	}

	/** Get the duration of the recording in milliseconds. */
	public int getDurationMsec() {
		return original.getDurationMsec() + respeaking.getDurationMsec();
	}

	/** Releases resources associated with the InterleavedPlayer. */
	public void release() {
		original.release();
		respeaking.release();
	}

	private void playOriginal() {
		Log.i("InterleavedPlayer", "playOriginal: " +
				getCurrentOriginalSegment());
		playSegment(getCurrentOriginalSegment(), original);
	}

	private Segment getCurrentOriginalSegment() {
		if (currentOriginalSegment == null) {
			advanceOriginalSegment();
		}
		return currentOriginalSegment;
	}

	private void playRespeaking() {
		Log.i("InterleavedPlayer", "playRespeaking: " +
				getCurrentRespeakingSegment());
		playSegment(getCurrentRespeakingSegment(), respeaking);
	}

	private Segment getCurrentRespeakingSegment() {
		return segments.getRespeakingSegment(getCurrentOriginalSegment());
	}

	private void playSegment(Segment segment, MarkedPlayer player) {
		if (segment != null) {
			player.seekTo(segment);
			player.setNotificationMarkerPosition(segment);
			player.play();
		}
	}

	private void advanceOriginalSegment() {
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

	private Iterator<Segment> getOriginalSegmentIterator() {
		if (originalSegmentIterator == null) {
			originalSegmentIterator = segments.getOriginalSegmentIterator();
		}
		return originalSegmentIterator;
	}

	private MarkedPlayer original;
	private MarkedPlayer respeaking;
	private Segments segments;
	private Segment currentOriginalSegment;
	private Iterator<Segment> originalSegmentIterator;
	private Player.OnCompletionListener onCompletionListener;

	private class OriginalMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			Log.i("InterleavedPlayer", "original marker reached");
			original.pause();
			playRespeaking();
		}
	}

	private class RespeakingMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			Log.i("InterleavedPlayer", "respeaking marker reached");
			respeaking.pause();
			advanceOriginalSegment();
			playOriginal();
		}
	}

	//////////////////////////////////////////////////////////////////////////

	/** Seek to a given point in the recording in milliseconds. */
	public void seekToMsec(int msec) {
		//Do nothing for now.
	}

	/** Set the callback to be run when the recording completes playing. */
	public void setOnCompletionListener(
			final Player.OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}
}
