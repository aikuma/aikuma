/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.record.ThumbRespeaker;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeakFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * Called to have the fragment instantiate it's user interface view.
	 *
	 * @param	inflater	
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.thumb_respeak_fragment, container, false);
		installButtonBehaviour(v);
		seekBar = (InterleavedSeekBar) v.findViewById(R.id.InterleavedSeekBar);
		seekBar.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {
					int originalProgress;
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							seekBar.setProgress(originalProgress);
						}
					}
					public void onStopTrackingTouch(SeekBar _seekBar) {};
					public void onStartTrackingTouch(SeekBar _seekBar) {
						originalProgress = seekBar.getProgress();
					};
				});
		seekBar.invalidate();
		return v;
	}

	/**
	 * Called when the fragment is destroyed; ensures resources are
	 * appropriately released.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (respeaker != null) {
			//If you hit the stop button really quickly, the player may not
			//have been initialized fully.
			respeaker.release();
		}
	}

	// Implements the behaviour for the play and respeak buttons.
	private void installButtonBehaviour(View v) {
		final ImageButton playButton = (ImageButton)
				v.findViewById(R.id.PlayButton);
		final ImageButton respeakButton = (ImageButton)
				v.findViewById(R.id.RespeakButton);

		playButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					respeaker.playOriginal();
					seekBarThread = new Thread(new Runnable() {
							public void run() {
								int currentPosition;
								while (true) {
									currentPosition =
											respeaker.getSimplePlayer().getCurrentMsec();
									seekBar.setProgress(
											(int)(((float)currentPosition/(float)
											respeaker.getSimplePlayer().getDurationMsec())*100));
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										return;
									}
								}
							}
					});
					seekBarThread.start();
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					respeaker.pauseOriginal();
					stopThread(seekBarThread);
				}
				return false;
			}
		});

		respeakButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					respeaker.pauseOriginal();
					respeaker.recordRespeaking();
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					try {
						respeaker.pauseRespeaking();
					} catch (MicException e) {
						ThumbRespeakFragment.this.getActivity().finish();
					}
				}
				return false;
			}
		});
	}

	// Wrapper to more safely stop threads.
	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
		}
	}

	/**
	 * Recording mutator.
	 *
	 * @param	recording	The recording to be played.
	 */
	public void setRecording(Recording recording) {
		this.recording = recording;
	}

	/**
	 * sample rate mutator.
	 *
	 * @param	sampleRate	The sample rate of the recording.
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * UUID mutator.
	 *
	 * @param	uuid	The uuid of the recording.
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * ThumbRespeaker mutator.
	 *
	 * @param	respeaker	The ThumbRespeaker to use.
	 */
	public void setThumbRespeaker(ThumbRespeaker respeaker) {
		this.respeaker = respeaker;
		respeaker.getSimplePlayer().setOnCompletionListener(onCompletionListener);
	}

	private Player.OnCompletionListener onCompletionListener =
			new Player.OnCompletionListener() {
				public void onCompletion(Player _player) {
					stopThread(seekBarThread);
					seekBar.setProgress(seekBar.getMax());
				}
			};
	private ThumbRespeaker respeaker;
	private ImageButton playButton;
	private ImageButton respeakButton;
	private InterleavedSeekBar seekBar;
	private Thread seekBarThread;
	private Recording recording;
	private UUID uuid;
	private int sampleRate;
}
