package org.lp20.aikuma.ui;

import android.app.Activity;
import android.os.Bundle;
import java.util.List;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;

public class ListenActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		List<Recording> recordings = Recording.readAll();
		if (recordings.size() >= 1) {
			setRecording(recording);
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
