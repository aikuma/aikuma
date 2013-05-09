package org.lp20.aikuma.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.R;

public class ListenFragment extends Fragment implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		try {
			player = new Player(
					((ListenActivity) getActivity()).getRecording());
			Player.OnCompletionListener listener =
					new Player.OnCompletionListener() {
						public void onCompletion(Player _player) {
							playPauseButton.setImageResource(R.drawable.play);
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
	public void onClick(View v) {
		if (v == playPauseButton) {
			if (player.isPlaying()) {
				pause();
			} else {
				play();
			}
		}
	}

	private void pause() {
		player.pause();
		playPauseButton.setImageResource(R.drawable.play);
	}

	private void play() {
		player.play();
		playPauseButton.setImageResource(R.drawable.pause);
	}

	private Player player;
	private ImageButton playPauseButton;
}
