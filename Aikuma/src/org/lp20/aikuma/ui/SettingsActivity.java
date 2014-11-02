/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


import java.io.IOException;
import org.lp20.aikuma.R;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

/**
 * The mother activity for settings - hosts buttons that link to various
 * specific settings activities.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SettingsActivity extends AikumaActivity {

	/**
	 * The default default sensitivity
	 */
	public static final int DEFAULT_DEFAULT_SENSITIVITY  = 4000;

	private final String TAG = "SettingsActivity";
	
	private SeekBar sensitivitySlider;
	private int defaultSensitivity;
	private SharedPreferences preferences;

	private boolean isBackupEnabled;
	private boolean isAutoDownloadEnabled;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}

	@Override
	public void onResume() {
		super.onResume();
		preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		readRespeakingMode();
		readRespeakingRewind();
		setupSensitivitySlider();
		setupSyncCheckBox();
	}
	
	private void setupSyncCheckBox() {
		CheckBox syncCheckBox = (CheckBox)
				findViewById(R.id.sync_checkBox);
		isBackupEnabled =
				preferences.getBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
		isAutoDownloadEnabled = 
				preferences.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
		
		syncCheckBox.setChecked(isBackupEnabled);
	}

	// Set the respeaking mode radio buttons as per the settings.
	private void readRespeakingMode() {
		String respeakingMode = preferences.getString(
				AikumaSettings.RESPEAKING_MODE_KEY, "thumb");
		RadioGroup radioGroup = (RadioGroup)
				findViewById(R.id.respeaking_radio_group);
		if (respeakingMode.equals("thumb")) {
			radioGroup.check(R.id.radio_thumb_respeaking);
		} else if (respeakingMode.equals("phone")) {
			radioGroup.check(R.id.radio_phone_respeaking);
		}
	}
	
	// Set up the respeaking rewind amount (rewind after each respeaking-segment)
	private void readRespeakingRewind() {
		int rewindAmount = preferences.getInt("respeaking_rewind", 500);
		EditText rewindAmountView = (EditText) findViewById(R.id.rewindAmount);
		rewindAmountView.setText(Integer.toString(rewindAmount));
	}

	private void writeRespeakingRewind() {
		EditText rewindAmountView = (EditText) findViewById(R.id.rewindAmount);
		int rewindAmount = Integer.parseInt(rewindAmountView.getText().toString());
		Editor prefsEditor = preferences.edit();
		prefsEditor.putInt("respeaking_rewind", rewindAmount);
		prefsEditor.commit();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			FileIO.writeDefaultSensitivity(defaultSensitivity);

			writeRespeakingRewind();
			Log.i(TAG, "wrote " + defaultSensitivity);
		} catch (IOException e) {
			//If it can't be written then just toast it.
			Toast.makeText(this, 
					"Failed to write default sensitivity setting to file", 
					Toast.LENGTH_LONG).show();
		}
	}

	// Define the sensitivity slider's functionality
	private void setupSensitivitySlider() {

		//Create the sensitivity slider functionality.
		sensitivitySlider = (SeekBar) findViewById(R.id.SensitivitySlider);
		//Read the sensitivity and set the slider accordingly.
		try {
			defaultSensitivity = FileIO.readDefaultSensitivity();
		} catch (IOException e) {
			defaultSensitivity = DEFAULT_DEFAULT_SENSITIVITY;
		}

		sensitivitySlider.setMax(DEFAULT_DEFAULT_SENSITIVITY*2);
		sensitivitySlider.setProgress(defaultSensitivity);

		sensitivitySlider.setOnSeekBarChangeListener(
			new OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar sensitivitySlider,
						int sensitivity, boolean fromUser) {
					if (sensitivity == 0) {
						defaultSensitivity = 1;
					} else {
						defaultSensitivity = sensitivity;
					}
				}
				public void onStartTrackingTouch(SeekBar seekBar) {}
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
	}

	/**
	 * Starts up the default languages activity.
	 *
	 * @param	view	The default language activity button.
	 */
	public void onDefaultLanguagesButton(View view) {
		Intent intent = new Intent(this, DefaultLanguagesActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts up the sync settings activity.
	 *
	 * @param	view	The sync settings activity button.
	 */
	public void onSyncSettingsButton(View view) {
		Intent intent = new Intent(this, SyncSettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * Adjusts the settings when the respeaking mode radio buttons are pressed.
	 *
	 * @param	radioButton	The radio button pressed
	 */
	public void onRespeakingRadioButtonClicked(View radioButton) {
		// Is the button now checked?
		boolean checked = ((RadioButton) radioButton).isChecked();
		// Allows us to edit the preferences
		Editor prefsEditor = preferences.edit();

		// Check which radio button was clicked
		switch (radioButton.getId()) {
			case R.id.radio_phone_respeaking:
				if (checked) {
					prefsEditor.putString(AikumaSettings.RESPEAKING_MODE_KEY, "phone");
					prefsEditor.commit();
				}
				break;
			case R.id.radio_thumb_respeaking:
				if (checked) {
					prefsEditor.putString(AikumaSettings.RESPEAKING_MODE_KEY, "thumb");
					prefsEditor.commit();
				}
				break;
		}
	}
	
	/**
	 * Adjusts the settings when the sync checkbox is checked.
	 * 
	 * @param checkBox	The checkbox 
	 */
	public void onSyncCheckBoxClicked(View checkBox) {
		boolean checked = ((CheckBox) checkBox).isChecked();
		Editor prefsEditor = preferences.edit();
		Log.i(TAG, "checkbox: " + checked);
		if(checked) {
			if(!isBackupEnabled) {
				Intent intent = new Intent(this, GoogleCloudService.class);
				intent.putExtra("id", "backup");
				startService(intent);
				isBackupEnabled = true;
			}
			if(!isAutoDownloadEnabled) {
				Intent intent = new Intent(this, GoogleCloudService.class);
				intent.putExtra("id", "autoDownload");
				startService(intent);
				isAutoDownloadEnabled = true;
			}
			
			prefsEditor.putBoolean(AikumaSettings.BACKUP_MODE_KEY, true);
			prefsEditor.putBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, true);
			prefsEditor.commit();
		} else {
			prefsEditor.putBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
			prefsEditor.putBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
			prefsEditor.commit();
		}
	}

}
