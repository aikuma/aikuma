/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;


import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.AikumaSettings;

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
	
	private SharedPreferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		readRespeakingRewind();
	}
	
	// Set up the respeaking rewind amount (rewind after each respeaking-segment)
	private void readRespeakingRewind() {
		int rewindAmount = preferences.getInt("respeaking_rewind", AikumaSettings.DEFAULT_RESPEAK_REWIND);
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
		writeRespeakingRewind();
	}
	
}
