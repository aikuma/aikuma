package org.lp20.aikuma.ui;

import android.util.Log;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.R;

public class ListenActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		//UUID respeakingUUID = UUID.randomUUID();
		//phoneRespeaker = new PhoneRespeaker(recording,
		//		new File(Recording.getRecordingsPath(),
		//				respeakingUUID.toString() + ".wav"),
		//		sampleRate);
		fragment =
				(ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		setUpPlayer();
	}

	private void setUpPlayer() {
		Intent intent = getIntent();
		UUID uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		try {
			recording = Recording.read(uuid);
			if (recording.isOriginal()) {
				setPlayer(new SimplePlayer(recording));
			} else {
				setPlayer(new InterleavedPlayer(recording));
			}
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			ListenActivity.this.finish();
		}
	}

	private void setPlayer(SimplePlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	private void setPlayer(InterleavedPlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onStop();
	}

	@Override
	public void onRestart() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onStop();
	}

	public void onPhoneRespeakingToggle(View view) {
		phoneRespeaking = ((ToggleButton) view).isChecked();
	}

	public void onThumbRespeakingButton(View view) {
		Intent intent = new Intent(this, ThumbRespeakActivity.class);
		intent.putExtra("uuidString", recording.getUUID().toString());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	private boolean phoneRespeaking = false;
	private Player player;
	private ListenFragment fragment;
	private Recording recording;
}
