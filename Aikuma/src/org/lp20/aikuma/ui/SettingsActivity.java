/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.UsageUtils;

/** The mother activity for settings - hosts buttons that link to various
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

	/**
	 * The slider that is used to control the default sensitivity.
	 */
	private SeekBar sensitivitySlider;

	/**
	 * The default sensitivity
	 */
	private int defaultSensitivity;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		getVersionInfo();
		getUsageInfo();
	}

	@Override
	public void onResume() {
		super.onResume();
		setupSensitivitySlider();
	}

	@Override
	public void onPause() {
		super.onResume();
		try {
			FileIO.writeDefaultSensitivity(defaultSensitivity);
			Log.i("132", "wrote " + defaultSensitivity);
		} catch (IOException e) {
			//If it can't be written then just toast it.
			Toast.makeText(this, 
					"Failed to write default sensitivity setting to file", 
					Toast.LENGTH_LONG).show();
		}
	}

	private void setupSensitivitySlider() {

		//Create the sensitivity slider functionality.
		sensitivitySlider = (SeekBar) findViewById(R.id.SensitivitySlider);
		//Read the sensitivity and set the slider accordingly.
		try {
			defaultSensitivity = FileIO.readDefaultSensitivity();
			Log.i("132", "read: " + defaultSensitivity);
		} catch (IOException e) {
			Log.i("132", "exception");
			defaultSensitivity = DEFAULT_DEFAULT_SENSITIVITY;
		}

		sensitivitySlider.setMax(DEFAULT_DEFAULT_SENSITIVITY*2);
		Log.i("132", "ds: " + defaultSensitivity);
		sensitivitySlider.setProgress(defaultSensitivity);
		Log.i("132", "ds2: " + defaultSensitivity);

		sensitivitySlider.setOnSeekBarChangeListener(
			new OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar sensitivitySlider,
						int sensitivity, boolean fromUser) {
					if (sensitivity == 0) {
						Log.i("132", "setting ds to 1");
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

	private void getVersionInfo() {
		TextView versionField = (TextView) findViewById(R.id.versionField);
		try {
				versionField.setText("Version: " +
						this.getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionName);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
				//Just leave the textview empty.
		}
	}

	private void getUsageInfo() {
		TextView usageField = (TextView) findViewById(R.id.usageField);
		usageField.setText("Hours used: " + UsageUtils.hoursUsed(16000, 16) + 
				"\nHours available: " + UsageUtils.hoursAvailable(16000, 16) +
				"\nOriginal recordings: " + UsageUtils.numOriginals() +
				"\nCommentaries: " + UsageUtils.numCommentaries());
	}

	public void onDefaultLanguagesButton(View view) {
		Intent intent = new Intent(this, DefaultLanguagesActivity.class);
		startActivity(intent);
	}

	public void onSyncSettingsButton(View view) {
		Intent intent = new Intent(this, SyncSettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
}
