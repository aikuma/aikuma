/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.service;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.AikumaSettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The broadcast-receiver listening to the connectivity-change event
 * Re-initialize the alaram-event of cloud-service to be executed peirodically
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class BootReceiver extends BroadcastReceiver {
	
	private final String TAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "BootReceiver started");
		if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			/*
			SharedPreferences preferences = 
					PreferenceManager.getDefaultSharedPreferences(context);
			
			AikumaSettings.isBackupEnabled = 
					preferences.getBoolean(AikumaSettings.BACKUP_MODE_KEY, true);
			AikumaSettings.isAutoDownloadEnabled = 
					preferences.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, true);
			*/

			if(AikumaSettings.isBackupEnabled && AikumaSettings.isAutoDownloadEnabled) {
				Intent serviceIntent = new Intent(context, GoogleCloudService.class);
				serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, "sync");
				serviceIntent.putExtra(GoogleCloudService.ACTION_EXTRA, "all");
				serviceIntent.putStringArrayListExtra(GoogleCloudService.ACCOUNT_KEY, 
						Aikuma.getGoogleAccounts());
				
				context.startService(serviceIntent);
			}	
			
        }
		
	}

}
