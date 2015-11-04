/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Recording.TagType;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingSpeakersActivity extends AikumaListActivity {

	private static final String TAG = RecordingSpeakersActivity.class.getCanonicalName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recordingspeakers);
		// Lets method in superclass know to ask user if they are willing to
		// discard new data on an activity transition via the menu.
		safeActivityTransition = false;
		safeActivityTransitionMessage = 
				"This will discard the selected speakers";
		
		okButton = (ImageButton) findViewById(R.id.recordingSpeakerOkButton);

		if(savedInstanceState == null)
			selectedSpeakers = new ArrayList<Speaker>();
		else {
			selectedSpeakers = savedInstanceState.getParcelableArrayList("selectedSpeakers");
		}

		speakers = Speaker.readAll(AikumaSettings.getCurrentUserId());
		Collections.sort(speakers);
		adapter = new RecordingSpeakerArrayAdapter(this, speakers, selectedSpeakers) {/*
			@Override
			// When checkbox in a listview is checked/unchecked
			public void updateActivityState() {
				updateOkButton();
				if(selectedSpeakers.size() > 0) {
					safeActivityTransition = true;
				}
			}*/
		};
		setListAdapter(adapter);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("selectedSpeakers", selectedSpeakers);
		
		//Call the superclass to save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Speaker speaker = (Speaker) intent.getParcelableExtra("speaker");
		if(speaker != null && !speakers.contains(speaker)) {
			speakers.add(speaker);
			Collections.sort(speakers);
			adapter.notifyDataSetChanged();
			selectedSpeakers.add(speaker);
		}
	}

	
	/**
	 * Starts the AddSpeakerActivity.
	 *
	 * @param	_view	The add-speaker button that was pressed.
	 */
	public void addSpeakerButtonPressed(View _view) {
		Intent intent = new Intent(this, AddSpeakerActivity1.class);
		intent.putExtra("origin", 1);
		startActivity(intent);
	}
	
	/**
	 * Create speaker tag files for the corresponding recording
	 * 
	 * @param 	_view	The ok-button pressed
	 */
	public void speakerOkButtonPressed(View _view) {
		ArrayList<String> tagVerIdList = new ArrayList<String>();
		String recordingId = getIntent().getStringExtra("recordingId");
		try {
			Recording recording = Recording.read(recordingId);
			String versionName = recording.getVersionName();
			
			for(Speaker speaker : selectedSpeakers) {
				String tagFileName = recording.tag(TagType.SPEAKER, 
						speaker.getId(), AikumaSettings.getCurrentUserId());
				if(tagFileName != null)
					tagVerIdList.add(versionName + "-" + tagFileName);
			}
		} catch (IOException e) {
			Log.e(TAG, "The recording can't be tagged: " + e.getMessage());
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
					Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			Toast.makeText(this, "The recording can't be tagged", Toast.LENGTH_LONG).show();
		}
		
		// If automatic-backup is enabled, archive this file
		if(AikumaSettings.isBackupEnabled) {
			Intent serviceIntent = new Intent(RecordingSpeakersActivity.this, 
					GoogleCloudService.class);
			serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "tag");
			serviceIntent.putStringArrayListExtra(GoogleCloudService.ACTION_EXTRA, tagVerIdList);
			serviceIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
					AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
					AikumaSettings.getCurrentUserToken());
			
			startService(serviceIntent);
		}		
		
		
		int startActivity = getIntent().getIntExtra("start", 0);
		Intent intent;
		if(startActivity == 0) {
			intent = new Intent(this, ListenActivity.class);
		} else {
			intent = new Intent(this, ListenRespeakingActivity.class);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		
		Toast.makeText(this, "Speaker tag added", Toast.LENGTH_LONG).show();
	}


	private ImageButton okButton;
	
	ArrayAdapter<Speaker> adapter;
	
	private List<Speaker> speakers;
	private ArrayList<Speaker> selectedSpeakers;
}
