package org.lp20.aikuma.audio;

import android.util.Log;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;

/**
 * Extends android.media.MediaPlayer to allow for the use of notification
 * markers in the same style as used by android.Media.AudioTrack.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian hanke	<florian.hanke@gmail.com>
 */
public class MarkedPlayer extends SimplePlayer {

	public MarkedPlayer(Recording recording,
			OnMarkerReachedListener listener) throws IOException {
		super(recording);
		setOnMarkerReachedListener(listener);
		notificationMarkerLoop = new Thread(new NotificationMarkerLoop());
		notificationMarkerLoop.start();
		unsetNotificationMarkerPosition();
	}

	/**
	 * Returns the marker position in milliseconds
	 *
	 * @return	marker position in milliseconds
	 */
	public int getNotificationMarkerPositionMsec() {
		return notificationMarkerPosition;
	}

	/**
	 * Sets the position of the notification marker.
	 *
	 * @param	notificationMarkerPosition	marker in milliseconds
	 */
	public void setNotificationMarkerPositionMsec(int notificationMarkerPosition) {
		this.notificationMarkerPosition = notificationMarkerPosition;
	}

	public void unsetNotificationMarkerPosition() {
		setNotificationMarkerPositionMsec(-1);
	}

	public void release() {
		super.release();
		stopNotificationMarkerLoop();
	}

	/**
	 * The class for listeners what would be called when set markers get
	 * reached.
	 */
	public static abstract class OnMarkerReachedListener {
		public abstract void onMarkerReached(MarkedPlayer p);
	}


	/**
	 * Sets the listener the MarkedPlayer notifies when a previously set
	 * marker is reached.
	 *
	 * @param	onMarkerReachedListener	the listener to be notified.
	 */
	private void setOnMarkerReachedListener(OnMarkerReachedListener
			onMarkerReachedListener) {
		this.onMarkerReachedListener = onMarkerReachedListener;
	}

	/** The listener that is notified when a set marker is reached */
	private OnMarkerReachedListener onMarkerReachedListener;

	private void stopNotificationMarkerLoop() {
		if (notificationMarkerLoop != null) {
			notificationMarkerLoop.interrupt();
		}
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
				if (notificationMarkerPosition >= 0) {
					if (getCurrentPositionMsec() >=
							getNotificationMarkerPositionMsec()) {
						onMarkerReachedListener.onMarkerReached(
								MarkedPlayer.this);
						unsetNotificationMarkerPosition();
					}
				}
			}
		}
	}

	/**
	 * The marker that determines when the playbackPositionUpdateListener gets
	 * called.
	 */
	private int notificationMarkerPosition;

	/**
	 * The thread that loops to check if markers have been reached.
	 */
	private Thread notificationMarkerLoop;


	//////////////////////////////////////////////////////////////////////////


	/*
	private boolean hasStartedPlaying = false;
	
	public void start() {
		//stopNotificationMarkerLoop();
		startNotificationMarkerLoop();
		mediaPlayer.start();
		if (!hasStartedPlaying) { hasStartedPlaying = true; }
	}
	
	public void pause() {
		if (hasStartedPlaying) {
			stopNotificationMarkerLoop();
			mediaPlayer.pause();
		}
	}

	public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
		mediaPlayer.setOnPreparedListener(listener);
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
			//If the thread is still alive, wait. This method should block
			//until the job is done.
			//while (notificationMarkerLoop.isAlive()) {}
		}
	}
	*/
}
