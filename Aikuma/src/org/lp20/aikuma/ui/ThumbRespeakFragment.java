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

public class ThumbRespeakFragment extends Fragment {

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
		View v = inflater.inflate(R.layout.thumb_respeak_fragment, container, false);
		installButtonBehaviour(v);
		seekBar = (InterleavedSeekBar) v.findViewById(R.id.InterleavedSeekBar);
		seekBar.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							respeaker.getSimplePlayer().seekToMsec((int)Math.round(
									(((float)progress)/100)*
									respeaker.getSimplePlayer().getDurationMsec()));
						}
					}
					public void onStopTrackingTouch(SeekBar _seekBar) {};
					public void onStartTrackingTouch(SeekBar _seekBar) {};
				});
		seekBar.invalidate();
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
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

	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
		}
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

	public void setThumbRespeaker(ThumbRespeaker respeaker) {
		this.respeaker = respeaker;
		respeaker.getSimplePlayer().setOnCompletionListener(onCompletionListener);
	}

	private void drawSegments(Segments segments) {
		Iterator<Segment> originalSegmentIterator =
				segments.getOriginalSegmentIterator();
		while (originalSegmentIterator.hasNext()) {
			Segment segment = originalSegmentIterator.next();
			float fraction =
					respeaker.getSimplePlayer().sampleToMsec(segment.getEndSample()) /
					(float) respeaker.getSimplePlayer().getDurationMsec();
			seekBar.addLine(fraction*100);
		}
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
