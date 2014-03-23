/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.R;

/**
 * A fragment used to perform audio playback; offers a seekbar, a play and
 * pause button
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenFragment extends Fragment implements OnClickListener {

	/**
	 * Called to have the ListenFragment instantiate it's interface view.
	 *
	 * @param	inflater	The LayoutInflater to inflate views in the
	 * fragment.
	 * @param	container	The parent view to attach the fragment view to.
	 * @param	savedInstanceState	Non-null if the fragment is being
	 * reconstructed.
	 *
	 * @return	The created view.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.listen_fragment, container, false);
		playPauseButton = (ImageButton) v.findViewById(R.id.PlayPauseButton);
		playPauseButton.setOnClickListener(this);
		seekBar = (InterleavedSeekBar) v.findViewById(R.id.InterleavedSeekBar);
		seekBar.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							player.seekToMsec((int)Math.round(
									(((float)progress)/100)*
									player.getDurationMsec()));
						}
					}
					public void onStopTrackingTouch(SeekBar _seekBar) {};
					public void onStartTrackingTouch(SeekBar _seekBar) {};
				});
		seekBar.invalidate();
		return v;
	}

	/**
	 * Called when the Fragment is obstructed by another view, or the activity has
	 * changed.
	 */
	@Override
	public void onPause() {
		super.onPause();
		pause();
	}

	/**
	 * Called when the Fragment is destroyed; ensures proper cleanup.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (player != null) {
			//If you hit the stop button really quickly, the player may not
			//have been initialized fully.
			player.release();
		}
	}

	/**
	 * Used to evaluate what the playPauseButton should do when clicked, as
	 * ListenFragment implements OnClickListener.
	 *
	 * @param	v	The view clicked
	 */
	@Override
	public void onClick(View v) {
		if (v == playPauseButton) {
			if (player.isPlaying()) {
				pause();
			} else {
				play();
			}
		}
	}

	/**
	 * A wrapper to Thread.interrupt() to prevent null-pointer
	 * exceptions.
	 *
	 * @param	thread	The thread to interrupt.
	 */
	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
		}
	}

	/**
	 * Pauses play of the audio and handles the GUI appropriately.
	 */
	private void pause() {
		player.pause();
		stopThread(seekBarThread);
		playPauseButton.setImageResource(R.drawable.play);
	}

	/**
	 * Plays the audio and handles the GUI appropriately.
	 */
	private void play() {
		player.play();
		seekBarThread = new Thread(new Runnable() {
				public void run() {
					int currentPosition;
					while (true) {
						currentPosition = player.getCurrentMsec();
						seekBar.setProgress(
								(int)(((float)currentPosition/(float)
								player.getDurationMsec())*100));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
		});
		seekBarThread.start();
		playPauseButton.setImageResource(R.drawable.pause);
	}

	/**
	 * Sets the player that the ListenFragment is to use.
	 *
	 * @param	simplePlayer	The simple player to be used.
	 */
	public void setPlayer(SimplePlayer simplePlayer) {
		this.player = simplePlayer;
		player.setOnCompletionListener(onCompletionListener);
	}

	/**
	 * Sets the player that the ListenFragment is to use
	 *
	 * @param	interleavedPlayer	The interleaved player to be used.
	 */
	public void setPlayer(InterleavedPlayer interleavedPlayer) {
		this.player = interleavedPlayer;
		Segments segments = new Segments(interleavedPlayer.getRecording());
		Iterator<Segment> originalSegmentIterator =
				segments.getOriginalSegmentIterator();
		while (originalSegmentIterator.hasNext()) {
			Segment segment = originalSegmentIterator.next();
			float fraction =
					player.sampleToMsec(segment.getEndSample()) /
					(float) player.getDurationMsec();
			seekBar.addLine(fraction*100);
		}
		player.setOnCompletionListener(onCompletionListener);
	}

	/** Defines behaviour for the fragment when a recording finishes playing.*/
	private Player.OnCompletionListener onCompletionListener =
			new Player.OnCompletionListener() {
				public void onCompletion(Player _player) {
					playPauseButton.setImageResource(R.drawable.play);
					stopThread(seekBarThread);
					seekBar.setProgress(seekBar.getMax());
				}
			};

	private Player player;
	private ImageButton playPauseButton;
	private InterleavedSeekBar seekBar;
	private Thread seekBarThread;
}
