package org.lp20.aikuma.ui;

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
import java.io.IOException;
import java.util.Iterator;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.R;

public class ListenFragment extends Fragment implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		try {
			recording = ((ListenActivity) getActivity()).getRecording();
			if (recording.isOriginal()) {
				player = new SimplePlayer(recording);
			} else {
				try {
					player = new InterleavedPlayer(recording);
					Segments segments = new Segments(recording.getUUID());
					Iterator<Segment> originalSegmentIterator =
							segments.getOriginalSegmentIterator();
					while (originalSegmentIterator.hasNext()) {
						Segment segment = originalSegmentIterator.next();
						float fraction =
								player.sampleToMsec(segment.getEndSample()) /
								(float) player.getDurationMsec();
						seekBar.addLine(fraction*100);
					}
				} catch (Exception e) {
					//URGENT. quit the pokemon exceptions and write a new class
					//that should get thrown by getOriginalUUID.
					getActivity().finish();
				}
			}
			Player.OnCompletionListener listener =
					new Player.OnCompletionListener() {
						public void onCompletion(Player _player) {
							playPauseButton.setImageResource(R.drawable.play);
							stopThread(seekBarThread);
							seekBar.setProgress(seekBar.getMax());
						}
					};
			player.setOnCompletionListener(listener);
		} catch (IOException e) {
			getActivity().finish();
			Toast.makeText(getActivity(), 
					"Cannot create a Player from the recording.", 
					Toast.LENGTH_LONG).show();
		}
		super.onActivityCreated(savedInstanceState);
	}

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

	@Override
	public void onPause() {
		super.onPause();
		pause();
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
		player.release();
	}

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

	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
		}
	}

	private void pause() {
		player.pause();
		stopThread(seekBarThread);
		playPauseButton.setImageResource(R.drawable.play);
	}

	private void play() {
		player.play();
		seekBarThread = new Thread(new Runnable() {
				public void run() {
					int currentPosition;
					while (true) {
						currentPosition = player.getCurrentPositionMsec();
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

	private Player player;
	private ImageButton playPauseButton;
	private InterleavedSeekBar seekBar;
	private Thread seekBarThread;
	private Recording recording;
}
