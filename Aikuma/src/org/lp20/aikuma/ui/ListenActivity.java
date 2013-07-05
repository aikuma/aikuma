package org.lp20.aikuma.ui;

import android.util.Log;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;

public class ListenActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		Intent intent = getIntent();
		UUID uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		try {
			setRecording(Recording.read(uuid));
		} catch (IOException e) {
			// In this case there probably isn't a Recording metadata file but
			// are we sure that's the exception? Probably should check for the
			// existence of the file.
			File wavFile = new File(Recording.getRecordingsPath(),
					uuid.toString() + ".wav");
		}
		ListenFragment fragment =
				(ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		fragment.setRecording(getRecording());
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

	public Recording getRecording() {
		return this.recording;
	}

	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	private Recording recording;
}
