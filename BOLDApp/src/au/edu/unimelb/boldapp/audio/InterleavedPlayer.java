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
	protected NewSegments segments;

	/**
	 * The Player for the original audio.
	 */
	protected SimplePlayer original;

	/**
	 * The Player for the respeaking audio.
	 */
	protected SimplePlayer respeaking;

	protected Iterator<Segment> originalSegmentIterator;

	protected Segment currentOriginalSegment;
  
  protected MediaPlayer.OnCompletionListener onCompletionListener;

	/**
	 * Standard Constructor; takes the UUID of the respeaking.
	 *
	 * @param	respeakingUUID	The UUID of the respeaking.
	 */
	public InterleavedPlayer(UUID respeakingUUID) throws Exception {
		initializePlayers(respeakingUUID);
    initializeSegments(respeakingUUID);
    initializeListeners();
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
		playOriginal();
	}

	/**
	 * Register a callback to be invoked when the end of a media source has
	 * been reached during playback.
	 *
	 * @param	listener	the callback that will be run.
	 */
	public void setOnCompletionListener(
			final MediaPlayer.OnCompletionListener onCompletionListener) {
    this.onCompletionListener = onCompletionListener;
  }

	/**
	 * Pauses playback; call start() to resume.
	 */
	public void pause() {
		original.pause();
		respeaking.pause();
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
  
	/**
	 * Gets the current playback position.
	 *
	 * @return the current position in milliseconds.
	 */
	public int getCurrentPosition() {
		return original.getCurrentPosition() + respeaking.getCurrentPosition();
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
	 * Releases resources associated with this Player.
	 */
	public void release() {
		original.release();
		respeaking.release();
	}
  
	protected void initializePlayers(UUID respeakingUUID) throws IOException {
		Recording respeakingMeta = FileIO.readRecording(respeakingUUID);
		UUID originalUUID = respeakingMeta.getOriginalUUID();
		original = new SimplePlayer(originalUUID, new
				OriginalMarkerReachedListener());
		respeaking = new SimplePlayer(respeakingUUID, new
				RespeakingMarkerReachedListener());
	}
  
  protected void initializeSegments(UUID respeakingUUID) {
    this.segments = new NewSegments(respeakingUUID);
  }
  
  protected void initializeListeners() {
		MediaPlayer.OnCompletionListener bothCompletedListener = new
		MediaPlayer.OnCompletionListener() {
			boolean completedOnce = false;
			@Override
			public void onCompletion(MediaPlayer _mp) {
				if (completedOnce) {
					if (onCompletionListener != null) { onCompletionListener.onCompletion(_mp); }
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

	protected void playOriginal() {
		playSegment(getCurrentOriginalSegment(), original);
	}
  
	protected void playRespeaking() {
		playSegment(getCurrentRespeakingSegment(), respeaking);
	}
  
  protected Segment getCurrentRespeakingSegment() {
    return segments.getRespeakingSegment(getCurrentOriginalSegment());
  }
  
	protected class OriginalMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedMediaPlayer p) {
			original.pause();
			playRespeaking();
		}
	}

	protected class RespeakingMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedMediaPlayer p) {
			respeaking.pause();
			advanceOriginalSegment();
			playOriginal();
		}
	}
  
	protected void playSegment(
			Segment segment, SimplePlayer player) {
		if (segment != null) {
			player.seekTo(segment);
			player.setNotificationMarkerPosition(segment);
			player.start();
		}
	}
  
	protected void advanceOriginalSegment() {
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
  
	protected Segment getCurrentOriginalSegment() {
		if (currentOriginalSegment == null) {
			advanceOriginalSegment();
		}
		return currentOriginalSegment;
	}
  
	protected Iterator<Segment> getOriginalSegmentIterator() {
		if (originalSegmentIterator == null) {
			originalSegmentIterator = segments.getOriginalSegmentIterator();
		}
		return originalSegmentIterator;
	}
  
}
