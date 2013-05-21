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
		initializeCompletionListeners();
	}

	private void initializeCompletionListeners() {
		Player.OnCompletionListener bothCompletedListener = new 
				Player.OnCompletionListener() {
			//boolean completedOnce = false;
			@Override
			public void onCompletion(Player p) {
				if (completedOnce) {
					if (onCompletionListener != null) {
						onCompletionListener.onCompletion(p);
					}
					reset();
					completedOnce = false;
				} else {
					completedOnce = true;
				}
			}
		};
		original.setOnCompletionListener(bothCompletedListener);
		respeaking.setOnCompletionListener(bothCompletedListener);
	}

	public void play() {
		playOriginal();
	}

	public void reset() {
		originalSegmentIterator = null;
		currentOriginalSegment = null;
		original.seekToSample(0l);
		respeaking.seekToSample(0l);
	}

	/** Indicates whether the recording is currently being played. */
	public boolean isPlaying() {
		return original.isPlaying() || respeaking.isPlaying();
	}

	/** Pauses the playback. */
	public void pause() {
		if (original.isPlaying()) {
			original.pause();
		}
		if (respeaking.isPlaying()) {
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
			original.pause();
			playRespeaking();
		}
	}

	private class RespeakingMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			respeaking.pause();
			if (!completedOnce) {
				advanceOriginalSegment();
				playOriginal();
			}
		}
	}

	private static boolean completedOnce = false;

	//////////////////////////////////////////////////////////////////////////

	/** Seek to a given point in the recording in milliseconds. */
	public void seekToMsec(int msec) {
		Log.i("InterleavedSeekTo", "Target: " + msec);
		Iterator<Segment> originalSegmentIterator =
				segments.getOriginalSegmentIterator();
		int total = 0;
		int prev_total = 0;
		//Initialized so that it has some value if we fail to get any
		//iterations of the loop
		Segment segment = new Segment(0l, 0l);
		while (originalSegmentIterator.hasNext()) {
			segment = originalSegmentIterator.next();
			prev_total = total;
			total += original.sampleToMsec(segment.getDuration());
			if (total > msec) {
				Log.i("InterleavedSeekTo", "Ending at original" +
						"segment.getEndSample: " +
						segment.getEndSample() + ", total: " + total +
						", segment.getStartSample: " +
						segment.getStartSample() + ", prev_total: " +
						prev_total + ", seeking to: " +
						(original.sampleToMsec(segment.getStartSample()) + 
						(msec - prev_total)));
				original.seekToMsec(
						original.sampleToMsec(segment.getStartSample()) + 
						(msec - prev_total));
				return;
			}
			segment = segments.getRespeakingSegment(segment);
			prev_total = total;
			total += respeaking.sampleToMsec(segment.getDuration());
			if (total > msec) {
				Log.i("InterleavedSeekTo", "Ending at respeaking" +
						"segment.getEndSample: " +
						segment.getEndSample() + ", total: " + total +
						", segment.getStartSample: " +
						segment.getStartSample() + ", prev_total: " +
						prev_total + ", seeking to: " +
						(respeaking.sampleToMsec(segment.getStartSample()) + 
						(msec - prev_total)));
				respeaking.seekToMsec(
						respeaking.sampleToMsec(segment.getStartSample()) + 
						(msec - prev_total));
				return;
			}
		}
		original.seekToMsec(
				original.sampleToMsec(segment.getEndSample()) + msec - total);
		Log.i("InterleavedSeekTo", "Ending at original, seeking to: " +
						(respeaking.sampleToMsec(segment.getEndSample()) + 
						(msec - total)));
	}

	/** Set the callback to be run when the recording completes playing. */
	public void setOnCompletionListener(
			final Player.OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}
}
