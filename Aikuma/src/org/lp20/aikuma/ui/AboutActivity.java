/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
//import android.view.View;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
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
		//setupUsageInfo();
		setupCloudInfo();
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
	
	private void setupCloudInfo() {
		int numOfUsers, numOfSpeakers, numOfItems = 0;
		long usedSpace, freeSpace;
		float cloudRatio = 0.0f, centralRatio = 0.0f;
		
		// Get the cloud-infomation
		numOfUsers = Aikuma.getGoogleAccounts().size();//AikumaSettings.setNumberOfUsers(Aikuma.getGoogleAccounts().size());
		numOfSpeakers = Speaker.readAll().size();//AikumaSettings.setNumberOfSpeakers(Speaker.readAll().size());
		numOfItems = UsageUtils.numOriginals();
		
		usedSpace = FileUtils.sizeOfDirectory(FileIO.getAppRootPath());
		freeSpace = FileIO.getAppRootPath().getUsableSpace();
		
		String googleEmailAccount = AikumaSettings.getCurrentUserId();
		List<Recording> recordings = Recording.readAll();
		if(googleEmailAccount != null) {
			SharedPreferences preferences = 
					getSharedPreferences(googleEmailAccount, MODE_PRIVATE);
			
			Set<String> approvedRecordingSet = (HashSet<String>)
					preferences.getStringSet(AikumaSettings.APPROVED_RECORDING_KEY, new HashSet<String>());
			Set<String> archivedRecordingSet = (HashSet<String>)
					preferences.getStringSet(AikumaSettings.ARCHIVED_RECORDING_KEY, new HashSet<String>());
			
			if (recordings.size() > 0) {
				cloudRatio = 100 * (approvedRecordingSet.size() + archivedRecordingSet.size()) / recordings.size();
				centralRatio = 100 * archivedRecordingSet.size() / recordings.size();
			}
		}		
		
		
		TextView cloudStatus = (TextView) findViewById(R.id.cloudStatus);
		StringBuilder sb = new StringBuilder();
		sb.append("Status:\n");
		sb.append(UsageUtils.getStorageFormat(usedSpace) + 
				" (" + UsageUtils.getTimeFormat(16000, 16, usedSpace) + ") used\n");
		sb.append(UsageUtils.getStorageFormat(freeSpace) + 
				" (" + UsageUtils.getTimeFormat(16000, 16, freeSpace) + ") available\n");
		sb.append(numOfUsers + " users\n");
		sb.append(numOfItems + " items\n");
		sb.append(cloudRatio + "% backed up to Google Drive\n");
		sb.append(centralRatio + "% archived centrally");
		
		cloudStatus.setText(sb);
	}

	
	
}
