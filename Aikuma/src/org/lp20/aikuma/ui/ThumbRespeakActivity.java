package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import org.lp20.aikuma.R;

public class ThumbRespeakActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		fragment = (ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
	}

	private void setUpThumbRespeaker() {
		Intent intent = getIntent();
		UUID originalUUID = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		respeakingUUID = UUID.randomUUID();
		try {
			recording = Recording.read(uuid);
			respeaker = new ThumbRespeaker(recording, respeakingUUID
		}
	}

	private ListenFragment fragment;
	private ThumbRespeaker respeaker;
	private UUID respeakingUUID;
}
