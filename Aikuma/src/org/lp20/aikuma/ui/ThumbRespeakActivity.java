/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.R;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.ThumbRespeaker;
import org.lp20.aikuma.model.Recording;

/**
 * The activity that allows recording to be respoken using buttons to
 * make explicit when playing and respeaking start and pause.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ThumbRespeakActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thumb_respeak);
		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = true;
		fragment = (ThumbRespeakFragment)
				getFragmentManager().findFragmentById(R.id.ThumbRespeakFragment);
		setUpThumbRespeaker();
		fragment.setThumbRespeaker(respeaker);
	}

	// Creates an appropriate ThumbRespeaker for this activity.
	private void setUpThumbRespeaker() {
		Intent intent = getIntent();
		id = (String)
				intent.getExtras().get("id");
		respeakingUUID = UUID.randomUUID();
		try {
			recording = Recording.read(id);
			respeaker = new ThumbRespeaker(recording, respeakingUUID);
		} catch (IOException e) {
			ThumbRespeakActivity.this.finish();
		} catch (MicException e) {
			ThumbRespeakActivity.this.finish();
		}
	}

	/**
	 * When the save respeaking button is called, stop the activity and send
	 * the relevant data to the RecordingMetadataActivity
	 *
	 * @param	view	The save respeaking button.
	 */
	public void onSaveRespeakingButton(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		intent.putExtra("uuidString", respeakingUUID.toString());
		intent.putExtra("sampleRate", recording.getSampleRate());
		intent.putExtra("originalId",
				Recording.getOriginalIdFromId(id));
		intent.putExtra("durationMsec", respeaker.getCurrentMsec());
		startActivity(intent);
		ThumbRespeakActivity.this.finish();
		try {
			respeaker.stop();
		} catch (MicException e) {
			Toast.makeText(this, "There has been an error stopping the microphone.",
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, "There has been an error writing the mapping between original and respeaking to file",
					Toast.LENGTH_LONG).show();
		}
	}

	private ThumbRespeakFragment fragment;
	private ThumbRespeaker respeaker;
	private String id;
	private UUID respeakingUUID;
	private Recording recording;
	private long sampleRate;
}
