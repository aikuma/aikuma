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

	private boolean hasStartedPlaying = false;

	public void seekTo(int msec) {
		mediaPlayer.seekTo(msec);
	}

	public void start() {
		startNotificationMarkerLoop();
		mediaPlayer.start();
		if (!hasStartedPlaying) { hasStartedPlaying = true; }
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	public int getDuration() {
		return mediaPlayer.getDuration();
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

	/*
	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(listener);
	}
	*/

	public void pause() {
		if (hasStartedPlaying) {
			stopNotificationMarkerLoop();
			mediaPlayer.pause();
		}
	}

	public void setOnCompletionListener(
			final MediaPlayer.OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _mp) {
				Log.i("segments2", " " + _mp.getCurrentPosition());
				if (_mp.getCurrentPosition() > 0) {
					stopNotificationMarkerLoop();
					onMarkerReachedListener.onMarkerReached(
							MarkedMediaPlayer.this);
					listener.onCompletion(mediaPlayer);
				}
			}
		});
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
	 * THIS IS TEMPORARY FOR DEBUGGING
	 */
	private static long msecToSample(int msec) {
		double sample = msec * (16000 / (float) 1000);
		return (long) sample;
	}

	/**
	 * Implements a run that polls the current position to check
	 * if a previously set marker has been reache
	 */
	private class NotificationMarkerLoop implements Runnable {

		public void run() {
			//while (getCurrentPosition() < getDuration()) {
			while (true) {
				try {
					Thread.sleep(100);
					//Log.i("segments", " " + ((long) sample));
				} catch (InterruptedException e) {
					// The player is being released so this thread should end.
					Log.e("segments", "Exception thingo", e);
					try {
					} catch (IllegalStateException ise) {
						// If we're getting this, then the player is just
						// releasing so we don't do anything.
					}
					return;
				}
				// If the marker is at zero, it's trivially low and the
				// callback shouldn't be called.
				if (notificationMarkerPosition > 0) {
							/*msecToSample(getDuration()));*/
					if (getCurrentPosition() >= 
							getNotificationMarkerPosition()) {
						Log.i("segments2", " " +
								msecToSample(getNotificationMarkerPosition()) + " " +
								msecToSample(getCurrentPosition()));
						Log.i("segments2", "marker reached");
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

	/**
	 * Standard constructor that starts a NotificationMarkerLoop once the
	 * MarkedMediaPlayer is prepared.
	 */
	public MarkedMediaPlayer(final OnMarkerReachedListener onMarkerReachedListener) {
		mediaPlayer = new MediaPlayer();
		setOnMarkerReachedListener(onMarkerReachedListener);
		setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer _) {
				/*
				notificationMarkerLoop = new Thread(
						new NotificationMarkerLoop(),
						"NotificationMarkerLoop");
				Log.i("segments", "starting the notification marker loop");
				notificationMarkerLoop.start();
				*/
			}
		});
	}

	/**
	 * Constructor for when the features of this MarkedMediaPlayer class will
	 * not be used
	 */
	public MarkedMediaPlayer(){
		//We don't set the listener, or register the NotificationMarkerLoop to
		//be run
		super();
	}


	public void release() {
		stopNotificationMarkerLoop();
		mediaPlayer.release();
	}

	public void unsetNotificationMarkerPosition() {
		setNotificationMarkerPosition(-1);
	}
}
