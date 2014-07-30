/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

import android.util.Log;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments.Segment;

/**
 * Extends android.media.MediaPlayer to allow for the use of notification
 * markers in the same style as used by android.Media.AudioTrack.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian hanke	<florian.hanke@gmail.com>
 */
public class MarkedPlayer extends SimplePlayer {

	/**
	 * Constructor
	 *
	 * @param	recording	The metadata of the recording to be played.
	 * @param	listener	The callback to call when a marker is reached.
	 * @param	playThroughSpeaker	True if the audio is to be played through the main
	 * speaker; false if through the ear piece (ie the private phone call style)
	 * @throws	IOException	If there is an issue reading from the data source.
	 */
	public MarkedPlayer(Recording recording, OnMarkerReachedListener listener,
			boolean playThroughSpeaker) throws IOException {
		super(recording, playThroughSpeaker);
		setOnMarkerReachedListener(listener);
		notificationMarkerLoop = new Thread(new NotificationMarkerLoop());
		notificationMarkerLoop.start();
		unsetNotificationMarkerPosition();
		count++;
	}

	/**
	 * Constructor
	 *
	 * @param	recording	The metadata of the recording to be played
	 * @throws	IOException	If an I/O issue occurs in the superconstructor.
	 * @param	playThroughSpeaker	True if the audio is to be played through the main
	 * speaker; false if through the ear piece (ie the private phone call style)
	 */
	public MarkedPlayer(Recording recording, boolean playThroughSpeaker) throws IOException {
		super(recording, true);
		notificationMarkerLoop = new Thread(new NotificationMarkerLoop());
		notificationMarkerLoop.start();
		unsetNotificationMarkerPosition();
		count++;
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
	public void setNotificationMarkerPositionMsec(
			int notificationMarkerPosition) {
		this.notificationMarkerPosition = notificationMarkerPosition;
	}

	/**
	 * Sets the notification marker to be at the end of the supplied segment
	 *
	 * @param	segment	The segment whose end is to be marked.
	 */
	public void setNotificationMarkerPosition(Segment segment) {
		if (segment != null) {
			setNotificationMarkerPositionMsec(
					sampleToMsec(segment.getEndSample()));
		} else {
			unsetNotificationMarkerPosition();
		}
	}

	/**
	 * Sets the notificaiton marker position to be at the specified sample.
	 *
	 * @param	sample	The sample at which the notification marker is to be
	 * set.
	 */
	public void setNotificationMarkerPositionSample(Long sample) {
		setNotificationMarkerPositionMsec(sampleToMsec(sample));
	}

	/**
	 * Removes the marker if it has been set.
	 */
	public void unsetNotificationMarkerPosition() {
		setNotificationMarkerPositionMsec(-1);
	}

	@Override
	public void release() {
		super.release();
		stopNotificationMarkerLoop();
	}

	/**
	 * Seeks to the beginning of the given segment.
	 *
	 * @param	segment	The segment to seek to.
	 */
	public void seekTo(Segment segment) {
		super.seekToSample(segment.getStartSample());
	}

	/**
	 * The class for listeners what would be called when set markers get
	 * reached.
	 */
	public static abstract class OnMarkerReachedListener {
		/**
		 * The method to be called when the set marker is reached.
		 *
		 * @param	p	The marked player whose marker has been reached.
		 */
		public abstract void onMarkerReached(MarkedPlayer p);
	}

	@Override
	public void setOnCompletionListener(final OnCompletionListener listener) {
		super.setOnCompletionListener(
				new Player.OnCompletionListener() {
					public void onCompletion(Player _p) {
						MarkedPlayer.this.onMarkerReachedListener.
								onMarkerReached(MarkedPlayer.this);
						listener.onCompletion(MarkedPlayer.this);
					}
				});
	}


	/**
	 * Sets the listener the MarkedPlayer notifies when a previously set
	 * marker is reached.
	 *
	 * @param	onMarkerReachedListener	the listener to be notified.
	 */
	public void setOnMarkerReachedListener(OnMarkerReachedListener
			onMarkerReachedListener) {
		this.onMarkerReachedListener = onMarkerReachedListener;
	}

	/** The listener that is notified when a set marker is reached */
	private OnMarkerReachedListener onMarkerReachedListener;

	// Stops the notification marker loop.
	private void stopNotificationMarkerLoop() {
		if (notificationMarkerLoop != null) {
			notificationMarkerLoop.interrupt();
		}
	}

	private static int count = 0;

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
				/* For later debugging 
				 * (This is commented out because of too many logs)
				Log.i(TAG, "notification marker position msec: " +
						getNotificationMarkerPositionMsec() +
						"\ngetCurentMsec(): " + getCurrentMsec());
				
				Log.i(TAG, "notification marker position sample: " +
						msecToSample(getNotificationMarkerPositionMsec()) +
						"\ngetCurentMsec() as sample: " +
						msecToSample(getCurrentMsec()));
				 */
				if (notificationMarkerPosition >= 0) {
					if (getCurrentMsec() >=
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

	private static final String TAG = "MarkedPlayer";

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
