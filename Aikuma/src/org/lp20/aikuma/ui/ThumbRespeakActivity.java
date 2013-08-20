package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.R;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.ThumbRespeaker;
import org.lp20.aikuma.model.Recording;

public class ThumbRespeakActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		fragment = (ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		setUpThumbRespeaker();
		fragment.setPlayer(respeaker.getSimplePlayer());
	}

	private void setUpThumbRespeaker() {
		Intent intent = getIntent();
		UUID originalUUID = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		respeakingUUID = UUID.randomUUID();
		try {
			recording = Recording.read(originalUUID);
			respeaker = new ThumbRespeaker(recording, respeakingUUID);
		} catch (IOException e) {
			ThumbRespeakActivity.this.finish();
		} catch (MicException e) {
			ThumbRespeakActivity.this.finish();
		}
	}

	private ListenFragment fragment;
	private ThumbRespeaker respeaker;
	private UUID respeakingUUID;
	private Recording recording;
}
