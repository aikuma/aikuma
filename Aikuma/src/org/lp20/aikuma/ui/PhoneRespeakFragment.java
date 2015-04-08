/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.media.AudioManager;
import android.content.Context;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import org.lp20.aikuma.audio.Audio;
import org.lp20.aikuma.audio.Beeper;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.record.PhoneRespeaker;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class PhoneRespeakFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.phone_respeak_fragment, container, false);
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

	@Override
	public void onPause() {
		super.onPause();
		haltRespeaking();
		this.proximityDetector.stop();
		Audio.reset(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		this.proximityDetector = new ProximityDetector(getActivity()) {
			public void near(float distance) {
				resumeRespeaking();
				/*
				if (!respeaker.getSimplePlayer().isPlaying()) {
					play();
				}
				*/
			}
			public void far(float distance) {
				Log.i("PhoneRespeak", "sleep: " + System.currentTimeMillis());
				try {
					Thread.sleep(AikumaSettings.EXTRA_AUDIO_DURATION);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.e("PhoneRespeak", "sleep");
				}
				Log.i("PhoneRespeak", "sleep: " + System.currentTimeMillis());
				
				haltRespeaking();
				/*
				if (respeaker.getSimplePlayer().isPlaying()) {
					pause();
				}
				*/
			}
		};
		this.proximityDetector.start();
		Audio.playThroughEarpiece(getActivity(), false);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (respeaker != null) {
			//If you hit the stop button really quickly, the player may not
			//have been initialized fully.
			respeaker.release();
		}
	}

	// A safer way to interrupt threads.
	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
		}
	}

	// Pauses the respaking process
	private void haltRespeaking() {
		respeaker.halt();
		stopThread(seekBarThread);
	}

	// Resumes the respeeaking process.
	private void resumeRespeaking() {
		respeaker.resume();
		seekBarThread = new Thread(new Runnable() {
				public void run() {
					int currentPosition;
					while (true) {
						currentPosition = respeaker.getSimplePlayer().getCurrentMsec();
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

	public void setRecording(Recording recording) {
		this.recording = recording;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * PhoneRespeaker mutator.
	 *
	 * @param	respeaker	The PhoneRespeaker
	 */
	public void setPhoneRespeaker(PhoneRespeaker respeaker) {
		this.respeaker = respeaker;
		respeaker.getSimplePlayer().setOnCompletionListener(onCompletionListener);
	}

	private Player.OnCompletionListener onCompletionListener =
			new Player.OnCompletionListener() {
				public void onCompletion(Player _player) {
					stopThread(seekBarThread);
					seekBar.setProgress(seekBar.getMax());
					Beeper.longBeep(getActivity(), null);
				}
			};

	private PhoneRespeaker respeaker;
	private ImageButton playPauseButton;
	private InterleavedSeekBar seekBar;
	private Thread seekBarThread;
	private Recording recording;
	private UUID uuid;
	private int sampleRate;
	private ProximityDetector proximityDetector;
}
