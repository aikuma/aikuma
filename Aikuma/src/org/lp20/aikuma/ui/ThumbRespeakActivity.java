package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
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
		setContentView(R.layout.thumb_respeak);
		fragment = (ThumbRespeakFragment)
				getFragmentManager().findFragmentById(R.id.ThumbRespeakFragment);
		setUpThumbRespeaker();
		fragment.setThumbRespeaker(respeaker);
	}

	private void setUpThumbRespeaker() {
		Intent intent = getIntent();
		originalUUID = UUID.fromString(
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


	public void onSaveRespeakingButton(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		intent.putExtra("uuidString", respeakingUUID.toString());
		intent.putExtra("sampleRate", recording.getSampleRate());
		intent.putExtra("originalUUIDString", originalUUID.toString());
		startActivity(intent);
		ThumbRespeakActivity.this.finish();
		try {
			respeaker.stop();
		} catch (MicException e) {
			//Maybe make a recording metadata file that refers to the error so
			//that the audio can be salvaged.
		}
	}

	private ThumbRespeakFragment fragment;
	private ThumbRespeaker respeaker;
	private UUID originalUUID;
	private UUID respeakingUUID;
	private Recording recording;
	private long sampleRate;
}
