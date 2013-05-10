package org.lp20.aikuma.audio;

import android.media.MediaPlayer;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;

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
	public InterleavedPlayer(Recording respeaking) throws IOException {
		if (recording.isOriginal()) {
			throw new IllegalArgumentException("The supplied Recording is " +
					"not a respeaking. Use SimplePlayer instead.");
		}
		original = new MarkedPlayer(
				new Recording.read(respeaking.getOriginalUUID()),
				new OriginalMarkerReachedListener());
		respeaking = new MarkedPlayer(respeaking,
				new RespeakingMarkerReachedListener());
		segments = new Segments(respeaking.getUUID());
	}

	public void play() {
		playOriginal();
	}

	private void playOriginal() {
		playSegment(getCurrentOriginalSegment(), original);
	}

	private Segment getCurrentOriginalSegment() {
		if (currentOriginalSegment == null) {
			advanceOriginalSegment();
		}
		return currentOriginalSegment;
	}

	private void playRespeaking() {
		playSegment(getCurrentRespeakingSegment(), respeaking);
	}

	private Segment getCurrentRespeakingSegment() {
		return segments.getRespeakingSegment(getCurrentOriginalSegment());
	}

	private void playSegment(Segment segment, MarkedPlayer player) {
		if (segment != null) {
			player.seekTo(segment);
			player.setNotificationMarkerPosition(segment);
			player.start();
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

	private SimplePlayer original;
	private SimplePlayer respeaking;
	private Segments segments;
	private Segment currentOriginalSegment;
	private Iterator<Segment> originalSegmentIterator;

	private class OriginalMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			original.pause();
			playRespeaking();
		}
	}

	private class RespeakingMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			respeaking.pause();
			advanceOriginalSegment();
			playOriginal();
		}
	}

	//////////////////////////////////////////////////////////////////////////

	/** Pauses the playback. */
	public void pause() {
		if (original.isPlaying()) {
			original.pause();
		} else if (respeaking.isPlaying()) {
			respeaking.pause();
		}
	}

	/** Indicates whether the recording is currently being played. */
	public boolean isPlaying() {
		return original.isPlaying() || respeaking.isPlaying();
	}

	/** Get current point in the recording in milliseconds. */
	public int getCurrentPositionMsec() {
		return original.getCurrentPosition() + respeaking.getCurrentPosition();
	}

	/** Get the duration of the recording in milliseconds. */
	public int getDurationMsec() {
		return original.getDurationMsec() + respeaking.getDurationMsec();
	}

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
