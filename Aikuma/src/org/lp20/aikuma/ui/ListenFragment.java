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
		ImageButton b = (ImageButton) v.findViewById(R.id.PlayButton);
		b.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.PlayButton:
				player.play();
				break;
		}
	}

	private Player player;

}
