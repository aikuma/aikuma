/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.service;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.AikumaSettings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * The broadcast-receiver listening for the device-boot event
 * Re-initialize the alaram-event of cloud-service to be executed peirodically
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class BootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			SharedPreferences preferences = 
					PreferenceManager.getDefaultSharedPreferences(context);
			boolean isBackupEnabled = 
					preferences.getBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
			boolean isAutoDownloadEnabled = 
					preferences.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
			
			if(isBackupEnabled && isAutoDownloadEnabled) {
				Intent serviceIntent = new Intent(context, GoogleCloudService.class);
				serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, "sync");
				serviceIntent.putStringArrayListExtra(GoogleCloudService.ACCOUNT_KEY, 
						Aikuma.getGoogleAccounts());
				
				PendingIntent pIntent = PendingIntent.getService(
						context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				
				AlarmManager alm = (AlarmManager) 
						context.getSystemService(Context.ALARM_SERVICE);
				alm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 
						1000, AikumaSettings.SYNC_INTERVAL, pIntent);
			}	
			
        }
		
	}

}
