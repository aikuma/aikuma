/*
	Copyright (C) 2013, The Aikuma Project
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

import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingSpeakersActivity extends AikumaListActivity {

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

		selectedSpeakers = getIntent().getParcelableArrayListExtra("selectedSpeakers");
		updateOkButton();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();

		speakers = Speaker.readAll(AikumaSettings.getCurrentUserId());
		ArrayAdapter<Speaker> adapter =
				new RecordingSpeakerArrayAdapter(
						this, speakers, selectedSpeakers) {
			@Override
			// When checkbox in a listview is checked/unchecked
			public void updateActivityState() {
				updateOkButton();
				if(selectedSpeakers.size() > 0) {
					safeActivityTransition = true;
				}
			}
		};
		setListAdapter(adapter);
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
	 * Return all speakers selected to RecordingMetadataActivity
	 * 
	 * @param 	_view	The ok-button pressed
	 */
	public void speakerOkButtonPressed(View _view) {
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra("speakers", selectedSpeakers);
		setResult(RESULT_OK, intent);
		this.finish();
	}
	
	/**
	 * Disables or enables the OK button if at least one language is selected
	 * used by LanguageArrayAdapter each time checkbox is checked
	 */
	private void updateOkButton() {
		if (selectedSpeakers.size() > 0) {
			okButton.setImageResource(R.drawable.ok_48);
			okButton.setEnabled(true);
			safeActivityTransition = true;
		} else {
			okButton.setImageResource(R.drawable.ok_disabled_48);
			okButton.setEnabled(false);
			safeActivityTransition = false;
		}
	}


	private ImageButton okButton;
	
	private List<Speaker> speakers;
	private ArrayList<Speaker> selectedSpeakers;
}
