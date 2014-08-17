/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lp20.aikuma.R;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Speaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author 	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingMetadataActivity3 extends AikumaListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata3);

		// Get metadata
		Intent intent = getIntent();
		uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		sampleRate = (Long) intent.getExtras().get("sampleRate");
		durationMsec = (Integer) intent.getExtras().get("durationMsec");
		groupId = (String)
				intent.getExtras().get("groupId");
		sourceId = (String)
				intent.getExtras().get("sourceId");
		numChannels = (Integer) intent.getExtras().get("numChannels");
		bitsPerSample = (Integer) intent.getExtras().get("bitsPerSample");
		latitude = (Double) intent.getExtras().get("latitude");
		longitude = (Double) intent.getExtras().get("longitude");
		format = (String)
				intent.getExtras().get("format");
		
		// settings for recording description
		description = (String) intent.getExtras().get("description");
		TextView descriptionView = (TextView) findViewById(R.id.description);
		descriptionView.setText(description);	
		
		// settings to add speakers to recording
		userImages =
				(LinearLayout) findViewById(R.id.userImagesAndAddUserButton);
		speakersIds = new ArrayList<String>();
		languages = new ArrayList<Language>();
		selectedLanguages = new ArrayList<Language>();
		
		// settings for OK button
		okButton = (ImageButton) findViewById(R.id.okButton3);
		updateOkButton();

		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		//if duration of the file > 250msec
		if(durationMsec > 250) {
			safeActivityTransition = true;
		}
		safeActivityTransitionMessage = "Are you sure you want to discard this recording?";
		

		adapter = new LanguagesArrayAdapter(this, languages, selectedLanguages) {
			@Override
			// When checkbox in a listview is checked/unchecked
			public void updateActivityState() {
				updateOkButton();
			}
		};
		setListAdapter(adapter);
	}


	/**
	 * Starts the AddSpeakerActivity.
	 *
	 * @param	view	The AddSpeakerButton
	 */
	public void onAddUserButtonPressed(View view) {
		Intent intent =
			new Intent(RecordingMetadataActivity3.this,
						RecordingSpeakersActivity.class);
		startActivityForResult(intent, ADD_SPEAKER);
	}


	// Used to recieve the results of taking photos and adding speakers.
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent intent) {
		if (requestCode == ADD_SPEAKER) {
			if (resultCode == RESULT_OK) {
				ArrayList<Speaker> speakers = intent.getParcelableArrayListExtra("speakers");
				
				//Speaker speaker = intent.getParcelableExtra("speaker");
				for(Speaker speaker : speakers) {
					if (!speakersIds.contains(speaker.getId())) {
						speakersIds.add(speaker.getId());
						for (Language language : speaker.getLanguages()) {
							if (!languages.contains(language)) {
								languages.add(language);
								adapter.notifyDataSetChanged();
							}
						}
						
						ImageView speakerImage = new ImageView(this);
						speakerImage.setAdjustViewBounds(true);
						speakerImage.setMaxHeight(60);
						speakerImage.setMaxWidth(60);
						//speakerImage.setPaddingRelative(5,5,5,5);
						try {
							speakerImage.setImageBitmap(speaker.getSmallImage());
						} catch (IOException e) {
							// If the image can't be loaded, we just leave it at that.
						}
						userImages.addView(speakerImage);
						recordingHasSpeaker = true;
						updateOkButton();
					}
				}
			}
		}
	}


	/**
	 * Called when the user has confirmed the recording
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity4.class);
		intent.putExtra("uuidString", uuid.toString());
		intent.putExtra("sampleRate", sampleRate);
		intent.putExtra("durationMsec", durationMsec);
		intent.putExtra("numChannels", numChannels);
		intent.putExtra("format", format);
		intent.putExtra("bitsPerSample", bitsPerSample);
		if(latitude != null && longitude != null) {
			// if location data is available, put else don't put
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
		}
		
		if(sourceId != null)
			intent.putExtra("sourceId", sourceId);
		if(groupId != null)
			intent.putExtra("groupId", groupId);
		
		intent.putExtra("description", description);
		intent.putStringArrayListExtra("speakersIds", speakersIds);
		intent.putParcelableArrayListExtra("languages", selectedLanguages);
		
		startActivity(intent);
	}
	

	/**
	 * Disables or enables the OK button depending on whether the recording now
	 * has a speaker and name.
	 * Turn on safeGoBackTransition when at least one thing is input by a user.
	 */
	private void updateOkButton() {
		if (recordingHasSpeaker && selectedLanguages.size() > 0) {
			okButton.setImageResource(R.drawable.ok_48);
			okButton.setEnabled(true);
		} else {
			okButton.setImageResource(R.drawable.ok_disabled_48);
			okButton.setEnabled(false);
		}
	}

	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private ArrayList<String> speakersIds;
	private List<Language> languages;
	private ArrayList<Language> selectedLanguages;
	private ArrayAdapter<Language> adapter;
	private LinearLayout userImages;
	private long sampleRate;
	private int durationMsec;
	private String groupId;
	private ImageButton okButton;
	private boolean recordingHasSpeaker;
	private String sourceId;
	private String format;
	private int bitsPerSample;
	private int numChannels;
	private String description;
	
	private Double latitude;
	private Double longitude;
}