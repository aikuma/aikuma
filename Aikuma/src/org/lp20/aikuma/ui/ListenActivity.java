package org.lp20.aikuma.ui;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
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
		try {
			setRecording(Recording.read(UUID.fromString(
					(String) intent.getExtras().get("uuidString"))));
		} catch (IOException e) {
			this.finish();
			Toast.makeText(this, "Cannot read specified recording.",
					Toast.LENGTH_LONG).show();
		}
	}

	public Recording getRecording() {
		return this.recording;
	}

	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	private Recording recording;
}
