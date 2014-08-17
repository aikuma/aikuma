/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.util.UUID;


import org.lp20.aikuma.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author 	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingMetadataActivity2 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata2);

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
		
		okButton = (ImageButton) findViewById(R.id.okButton2);
		updateOkButton();

		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		//if duration of the file > 250msec
		if(durationMsec > 250) {
			safeActivityTransition = true;
		}
		safeActivityTransitionMessage = "Are you sure you want to discard this recording?";
		
		// EditText for the recording name
		nameField = (EditText) findViewById(R.id.recordingDescription);
		nameField.addTextChangedListener(emptyTextWatcher);
	}

	/**
	 * Called when the user has confirmed the recording
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity3.class);
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
		
		String description = nameField.getText().toString();
		intent.putExtra("description", description);
		
		startActivity(intent);
	}


	// Used to check whether the recording name is long enough based on what
	// the user is entered, and disable/enable the okButton on the fly.
	private TextWatcher emptyTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s,
				int start, int count, int after) {
		}
		public void onTextChanged(CharSequence s,
				int start, int before, int count) {
			if (s.length() == 0) {
				recordingHasName = false;
				updateOkButton();
			} else {
				recordingHasName = true;
				updateOkButton();
			}
		}
	};

	/**
	 * Disables or enables the OK button depending on whether the recording now
	 * has a name.
	 */
	private void updateOkButton() {
		if (recordingHasName) {
			okButton.setImageResource(R.drawable.ok_48);
			okButton.setEnabled(true);
		} else {
			okButton.setImageResource(R.drawable.ok_disabled_48);
			okButton.setEnabled(false);
		}
	}

	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private long sampleRate;
	private int durationMsec;
	private String groupId;
	private EditText nameField;
	private ImageButton okButton;
	private boolean recordingHasName;
	private String sourceId;
	private String format;
	private int bitsPerSample;
	private int numChannels;
	
	private Double latitude;
	private Double longitude;
}