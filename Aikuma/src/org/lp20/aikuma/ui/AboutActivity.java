/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.os.Bundle;
//import android.view.View;
import android.widget.TextView;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.UsageUtils;

/**
 * An activity that gives various information about the app including version
 * number and usage statistics.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class AboutActivity extends AikumaActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setupVersionInfo();
		setupAndroidIdInfo();
		setupUsageInfo();
	}

	// Retrieves information about the Aikuma version.
	private void setupVersionInfo() {
		TextView versionField = (TextView) findViewById(R.id.versionField);
		try {
				versionField.setText("Version: " +
						this.getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionName);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			//Just leave the textview empty.
		}
	}

	// Displays the android ID.
	private void setupAndroidIdInfo() {
		TextView androidId = (TextView) findViewById(R.id.androidIdField);
		androidId.setText("Android ID: " + Aikuma.getAndroidID());
	}

	// Retrievs information about the user and displays it.
	private void setupUsageInfo() {
		TextView usageField = (TextView) findViewById(R.id.usageField);
		usageField.setText("Recording time used: " + UsageUtils.timeUsed(16000, 16) + 
				"\nRecording time available: " + UsageUtils.timeAvailable(16000, 16) +
				"\nOriginal recordings: " + UsageUtils.numOriginals() +
				"\nCommentaries: " + UsageUtils.numCommentaries());
	}


}
