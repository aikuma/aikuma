/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.Recorder;
import org.lp20.aikuma.audio.record.ThumbRespeaker;
import org.lp20.aikuma.model.Language;
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
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		menuBehaviour = new MenuBehaviour(this);
		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = true;
		fragment = (ThumbRespeakFragment)
				getFragmentManager().findFragmentById(R.id.ThumbRespeakFragment);
		setUpThumbRespeaker();
		fragment.setThumbRespeaker(respeaker);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = getIntent();
		String respeakingType = intent.getStringExtra("respeakingType");
		selectedLanguages = intent.getParcelableArrayListExtra("languages");
		String firstLang = "";
		if(selectedLanguages.size() > 0)
			firstLang = "(" + selectedLanguages.get(0).getCode() + ")";
		boolean isMenu = menuBehaviour.onCreateOptionsMenu(menu);
		if(respeakingType.equals("respeak"))
			menuBehaviour.addItem(R.drawable.respeak_32, "respeak" + firstLang);
		else
			menuBehaviour.addItem(R.drawable.translate_32, "interpret" + firstLang);
		return isMenu;
	}
	
	// Creates an appropriate ThumbRespeaker for this activity.
	private void setUpThumbRespeaker() {
		Intent intent = getIntent();
		sourceId = (String) intent.getExtras().get("sourceId");
		int rewindAmount = intent.getExtras().getInt("rewindAmount");
		ownerId = (String) intent.getExtras().get("ownerId");
		versionName = (String) intent.getExtras().get("versionName");
		
		respeakingUUID = UUID.randomUUID();
		
		try {
			recording = Recording.read(versionName, ownerId, sourceId);
			respeaker = new ThumbRespeaker(recording, respeakingUUID, rewindAmount);
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
		
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				respeaker.saveRespeaking();
				
				Intent intent = new Intent(ThumbRespeakActivity.this, RecordingMetadataActivity1.class);
				intent.putExtra("uuidString", respeakingUUID.toString());
				intent.putExtra("sampleRate", recording.getSampleRate());
				intent.putExtra("sourceVerId", 
						recording.getVersionName() + "-" + recording.getId());
				intent.putExtra("groupId",
						Recording.getGroupIdFromId(sourceId));
				intent.putExtra("durationMsec", respeaker.getCurrentMsec());
				Recorder recorder = respeaker.getRecorder();
				intent.putExtra("numChannels", recorder.getNumChannels());
				intent.putExtra("format", recorder.getFormat());
				intent.putExtra("bitsPerSample", recorder.getBitsPerSample());
				intent.putParcelableArrayListExtra("languages", selectedLanguages);
				startActivity(intent);
				ThumbRespeakActivity.this.finish();
				try {
					respeaker.stop();
				} catch (MicException e) {
					Toast.makeText(ThumbRespeakActivity.this, "There has been an error stopping the microphone.",
							Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(ThumbRespeakActivity.this, "There has been an error writing the mapping between original and respeaking to file",
							Toast.LENGTH_LONG).show();
				}
			}
		};
		
		Aikuma.showConfirmationDialog(this, "Did you finish the recording", listener);
	}

	private ThumbRespeakFragment fragment;
	private ThumbRespeaker respeaker;
	private String sourceId;
	private String ownerId;
	private String versionName;
	private UUID respeakingUUID;
	private Recording recording;
	private long sampleRate;
	private ArrayList<Language> selectedLanguages;
}
