package au.edu.unimelb.aikuma.audio;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;

/**
 * Extends android.media.MediaPlayer to allow for the use of notification
 * markers in the same style as used by android.Media.AudioTrack.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian hanke	<florian.hanke@gmail.com>
 */
public class MarkedMediaPlayer {

	MediaPlayer mediaPlayer;
	
	/**
	 * The thread that loops to check if markers have been reached.
	 */
	private Thread notificationMarkerLoop;

	/**
	 * The marker that determines when the playbackPositionUpdateListener gets
	 * called.
	 */
	private int notificationMarkerPosition;

	/**
	 * The listener the MarkedMediaPlayer notifies when a previously set marker
	 * is reached.
	 */
	private OnMarkerReachedListener onMarkerReachedListener;
	
	private boolean hasStartedPlaying = false;
	
	public MarkedMediaPlayer() {
		super();
		mediaPlayer = new MediaPlayer();
	}
	
	/**
	 * Standard constructor that starts a NotificationMarkerLoop once the
	 * MarkedMediaPlayer is prepared.
	 */
	public MarkedMediaPlayer(final OnMarkerReachedListener onMarkerReachedListener) {
		this();
		setOnMarkerReachedListener(onMarkerReachedListener);
	}

	public void start() {
		startNotificationMarkerLoop();
		mediaPlayer.start();
		if (!hasStartedPlaying) { hasStartedPlaying = true; }
	}
	
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
	
	public void pause() {
		if (hasStartedPlaying) {
			stopNotificationMarkerLoop();
			mediaPlayer.pause();
		}
	}

	public void seekTo(int msec) {
		mediaPlayer.seekTo(msec);
	}

	public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
		mediaPlayer.setOnPreparedListener(listener);
	}

	public void setDataSource(String path) throws IOException {
		mediaPlayer.setDataSource(path);
	}

	public void prepare() throws IOException {
		mediaPlayer.prepare();
	}
	
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		return mediaPlayer.getDuration();
	}
	
	public void setOnCompletionListener(
			final MediaPlayer.OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _mp) {
				if (_mp.getCurrentPosition() > 0) {
					stopNotificationMarkerLoop();
					if (onMarkerReachedListener != null) {
						onMarkerReachedListener.onMarkerReached(
								MarkedMediaPlayer.this);
					}
					listener.onCompletion(mediaPlayer);
				}
			}
		});
	}

	/**
	 * Returns the marker position in milliseconds
	 *
	 * @return	marker position in milliseconds
	 */
	public int getNotificationMarkerPosition() {
		return notificationMarkerPosition;
	}

	/**
	 * Sets the position of the notification marker.
	 *
	 * @param	notificationMarkerPosition	marker in milliseconds
	 */
	public void setNotificationMarkerPosition(int notificationMarkerPosition) {
		this.notificationMarkerPosition = notificationMarkerPosition;
	}

	/**
	 * Sets the listener the MarkedMediaPlayer notifies when a previously set
	 * marker is reached.
	 *
	 * @param	onMarkerReachedListener	the listener to be notified.
	 */
	public void setOnMarkerReachedListener( OnMarkerReachedListener
			onMarkerReachedListener) {
		this.onMarkerReachedListener = onMarkerReachedListener;
	}

	/**
	 * The class for listeners what would be called when set markers get
	 * reached.
	 */
	public static abstract class OnMarkerReachedListener {
		public abstract void onMarkerReached(MarkedMediaPlayer p);
	}

	/**
	 * Implements a run that polls the current position to check
	 * if a previously set marker has been reache
	 */
	private class NotificationMarkerLoop implements Runnable {

		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// The player is being released so this thread should end.
					break;
				}
				// If the marker is at zero, it's trivially low and the
				// callback shouldn't be called.
				if (notificationMarkerPosition >= 0) {
					if (getCurrentPosition() >= getNotificationMarkerPosition()) {
						if(onMarkerReachedListener != null) {
							onMarkerReachedListener.onMarkerReached(
									MarkedMediaPlayer.this);
							unsetNotificationMarkerPosition();
						}
					}
				}
			}
		}
	}

	public void release() {
		stopNotificationMarkerLoop();
		mediaPlayer.release();
	}

	private void unsetNotificationMarkerPosition() {
		setNotificationMarkerPosition(-1);
	}
	
	private void startNotificationMarkerLoop() {
		notificationMarkerLoop = new Thread(
				new NotificationMarkerLoop(),
				"NotificationMarkerLoop");
		notificationMarkerLoop.start();
	}

	private void stopNotificationMarkerLoop() {
		if (notificationMarkerLoop != null) {
			notificationMarkerLoop.interrupt();
		}
	}
}
