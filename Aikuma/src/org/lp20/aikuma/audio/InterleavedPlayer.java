package org.lp20.aikuma.audio;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;
import java.util.Iterator;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;

/**
 * A player that plays both the original recording and its respeaking in an
 * interleaved fashion, whereby a segment of the original is played, followed
 * by the corresponding segment of the respeaking, followed by the next
 * original segment and so on.
 *
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
	public InterleavedPlayer(Recording recording) throws IOException {
		setRecording(recording);
		if (recording.isOriginal()) {
			throw new IllegalArgumentException("The supplied Recording is " +
					"not a respeaking. Use SimplePlayer instead.");
		}
		setSampleRate(recording.getSampleRate());
		original = new MarkedPlayer(
				Recording.read(recording.getOriginalUUID()),
				new OriginalMarkerReachedListener(), true);
		respeaking = new MarkedPlayer(recording,
				new RespeakingMarkerReachedListener(), true);
		segments = new Segments(recording.getUUID());
		initializeCompletionListeners();
		completedOnce = false;
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
	public int getCurrentMsec() {
		return original.getCurrentMsec();
	}

	/** Get the duration of the recording in milliseconds. */
	public int getDurationMsec() {
		return original.getDurationMsec();
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
	private Recording recording;

	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	public Recording getRecording() {
		return this.recording;
	}

	/**
	 * Implements a method for the original audio source to call when an
	 * original segment has finished playing.
	 */
	private class OriginalMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			Log.i("release", "original onMarker reached, completedOnce = " +
					completedOnce);
			original.pause();
			playRespeaking();
		}
	}

	/**
	 * Implements a method for the respeaking audio source to call when a
	 * respeaking segment has finished playing.
	 */
	private class RespeakingMarkerReachedListener extends
			MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			Log.i("release", "respeaking onMarker reached, completedOnce = " +
					completedOnce);
			respeaking.pause();
			if (!completedOnce) {
				advanceOriginalSegment();
				playOriginal();
			}
		}
	}

	public long getSampleRate() {
		//If the sample rate is less than zero, then this indicates that there
		//wasn't a sample rate found in the metadata file.
		if (sampleRate <= 0l) {
			throw new RuntimeException(
					"The sampleRate of the recording is not known.");
		}
		return sampleRate;
	}

	private void setSampleRate(long sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int sampleToMsec(long sample) {
		long msec = sample / (getSampleRate() / 1000);
		if (msec > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) msec;
	}

	private static boolean completedOnce = false;
	private long sampleRate;

	//////////////////////////////////////////////////////////////////////////

	/** Seek to a given point in the recording in milliseconds. */
	public void seekToMsec(int msec) {
		original.seekToMsec(msec);
	}

	/** Set the callback to be run when the recording completes playing. */
	public void setOnCompletionListener(
			final Player.OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}
}
