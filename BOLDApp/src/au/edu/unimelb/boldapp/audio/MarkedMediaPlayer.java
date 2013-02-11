package au.edu.unimelb.aikuma.audio;

import android.util.Log;

import android.media.MediaPlayer;

/**
 * Extends android.media.MediaPlayer to allow for the use of notification
 * markers in the same style as used by android.Media.AudioTrack.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian hanke	<florian.hanke@gmail.com>
 */
public class MarkedMediaPlayer extends MediaPlayer {

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
	
	private static int count;

	/**
	 * Implements a run that polls the current position to check
	 * if a previously set marker has been reache
	 */
	private class NotificationMarkerLoop implements Runnable {
		private int position;

		public void run() {
			while (getCurrentPosition() < getDuration()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// The player is being released so this thread should end.
					return;
				}
				// If the marker is at zero, it's trivially low and the
				// callback shouldn't be called.
				if (notificationMarkerPosition != 0) {
					if (getCurrentPosition() >= 
							getNotificationMarkerPosition()) {
						Log.i("mark", "marker reached");
						if(onMarkerReachedListener != null) {
							Log.i("segCount", " " + count++);
							onMarkerReachedListener.onMarkerReached(
									MarkedMediaPlayer.this);
						}
					}
				}
			}
		}
	}

	/**
	 * Standard constructor that starts a NotificationMarkerLoop once the
	 * MarkedMediaPlayer is prepared.
	 */
	public MarkedMediaPlayer(OnMarkerReachedListener onMarkerReachedListener) {
		super();
		setOnMarkerReachedListener(onMarkerReachedListener);
		setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer _) {
				notificationMarkerLoop = new Thread(
						new NotificationMarkerLoop(),
						"NotificationMarkerLoop");
				notificationMarkerLoop.start();
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


	@Override
	public void release() {
		// Destroy the notificationMarkerLoop thread.
		if (this.notificationMarkerLoop != null) {
			this.notificationMarkerLoop.interrupt();
		}
		super.release();
	}
}
