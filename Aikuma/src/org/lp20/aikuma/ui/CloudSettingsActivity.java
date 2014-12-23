package org.lp20.aikuma.ui;

import java.io.IOException;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.service.BootReceiver;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

/**
 * The activity for cloud-sync settings.
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class CloudSettingsActivity extends AikumaActivity {

	private final String TAG = "CloudSettingsActivity";

	private SharedPreferences preferences;
	
	private Editor prefsEditor;
	
	private CheckBox wifiCheckBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_settings);
		preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		prefsEditor = preferences.edit();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		setupSyncCheckBox();
		setupWifiCheckBox();
	}
	
	private void setupSyncCheckBox() {
		CheckBox syncCheckBox = (CheckBox)
				findViewById(R.id.sync_checkBox);
		if(AikumaSettings.getCurrentUserId() == null) {
			syncCheckBox.setEnabled(false);
		}
		AikumaSettings.isBackupEnabled =
				preferences.getBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
		AikumaSettings.isAutoDownloadEnabled = 
				preferences.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
		
		syncCheckBox.setChecked(AikumaSettings.isBackupEnabled);
	}
	
	private void setupWifiCheckBox() {
		wifiCheckBox = (CheckBox)
				findViewById(R.id.wifi_checkBox);
		if(AikumaSettings.getCurrentUserId() == null || 
				!AikumaSettings.isBackupEnabled || !AikumaSettings.isAutoDownloadEnabled) {
			wifiCheckBox.setEnabled(false);
		}
		AikumaSettings.isOnlyWifi =
				preferences.getBoolean(AikumaSettings.WIFI_MODE_KEY, true);
		
		wifiCheckBox.setChecked(!AikumaSettings.isOnlyWifi);
	}

	/**
	 * Adjusts the settings when the sync checkbox is checked.
	 * 
	 * @param checkBox	The checkbox 
	 */
	public void onSyncCheckBoxClicked(View checkBox) {
		boolean checked = ((CheckBox) checkBox).isChecked();
		Log.i(TAG, "sync-checkbox: " + checked);
		if(checked) {
			if(!AikumaSettings.isBackupEnabled && !AikumaSettings.isAutoDownloadEnabled) {
				Intent intent = new Intent(this, GoogleCloudService.class);
				intent.putExtra(GoogleCloudService.ACTION_KEY, "sync");
				intent.putStringArrayListExtra(GoogleCloudService.ACCOUNT_KEY, 
						Aikuma.getGoogleAccounts());
				
				startService(intent);
			}
			
			AikumaSettings.isBackupEnabled = true;
			AikumaSettings.isAutoDownloadEnabled = true;
			prefsEditor.putBoolean(AikumaSettings.BACKUP_MODE_KEY, true);
			prefsEditor.putBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, true);
			prefsEditor.commit();
			
			wifiCheckBox.setEnabled(true);
		} else {
			AikumaSettings.isBackupEnabled = false;
			AikumaSettings.isAutoDownloadEnabled = false;
			AikumaSettings.isOnlyWifi = true;
			prefsEditor.putBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
			prefsEditor.putBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
			prefsEditor.putBoolean(AikumaSettings.WIFI_MODE_KEY, true);
			prefsEditor.commit();
			
			wifiCheckBox.setChecked(false);
			wifiCheckBox.setEnabled(false);
		}
	}
	
	/**
	 * Callback function for the checkbox allowing sync over cellular network
	 * 
	 * @param checkBox	Unchecked(default): allow sync only over wifi-network
	 */
	public void onWifiCheckBoxClicked(View checkBox) {
		boolean checked = ((CheckBox) checkBox).isChecked();
		Log.i(TAG, "wifi-checkbox: " + checked);
		if(checked) {
			AikumaSettings.isOnlyWifi = false;
			prefsEditor.putBoolean(AikumaSettings.WIFI_MODE_KEY, false);
			prefsEditor.commit();
		} else {
			AikumaSettings.isOnlyWifi = true;
			prefsEditor.putBoolean(AikumaSettings.WIFI_MODE_KEY, true);
			prefsEditor.commit();
		}

	}
}
