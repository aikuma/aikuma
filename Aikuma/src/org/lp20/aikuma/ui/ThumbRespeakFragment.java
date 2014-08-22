/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import org.lp20.aikuma.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeakFragment extends Fragment {

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
		final int greyColor = 0xffd6d6d6;
		playButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Color change
					//playButton.setBackgroundColor(0xff00d500);
					int previousGestureTime = gestureTime;
					gestureTime = respeaker.getSimplePlayer().getCurrentMsec();
					gestureTimeUpToDown = System.currentTimeMillis() - gestureTimeUpToDown;
					
					if(isCommented){ // After commentary is recorded(or At the start)
						//Rewind and Store the start-point
						respeaker.playOriginal(1); 
					} else { // After commentary is recorded(or At the start) and 
							 // green-arrow button is pressed more than once
						if(previousGestureTime < VALID_GESTURE_TIME) {
							respeaker.playOriginal(2); //Rewind
						} else if(gestureTimeUpToDown < VALID_GESTURE_TIME) {
							respeaker.playOriginal(0); //Continue
						} else {
							respeaker.playOriginal(1); //Rewind and Store the start-point
						}
					}
					
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
					//playButton.setBackgroundColor(greyColor);
					respeaker.pauseOriginal();
					stopThread(seekBarThread);
					gestureTime = (respeaker.getSimplePlayer().getCurrentMsec() - gestureTime);
					gestureTimeUpToDown = System.currentTimeMillis();
					Log.i("Thumb", ""+ gestureTime);
					if(gestureTime >= VALID_GESTURE_TIME) {
						isCommented = false;
					}
				}
				return false;
			}
		});

		respeakButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(!isCommented) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						//respeakButton.setBackgroundColor(0xffff2020);
						respeaker.pauseOriginal();
						respeaker.recordRespeaking();
					}
					if (event.getAction() == MotionEvent.ACTION_UP) {
						//respeakButton.setBackgroundColor(greyColor);
						try {
							respeaker.pauseRespeaking();
							isCommented = true;
						} catch (MicException e) {
							ThumbRespeakFragment.this.getActivity().finish();
						}
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
	
	private final int VALID_GESTURE_TIME = 100; //0.1sec
	private int gestureTime = VALID_GESTURE_TIME;
	private long gestureTimeUpToDown = VALID_GESTURE_TIME;
	private boolean isCommented = true;
}
