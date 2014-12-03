/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.service;

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
			
			Intent serviceIntent = new Intent(context, GoogleCloudService.class);
			serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, "sync");
			serviceIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
					preferences.getString(AikumaSettings.SETTING_OWNER_ID_KEY, null));
			serviceIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
					preferences.getString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, null));
			
			PendingIntent pIntent = PendingIntent.getService(
					context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			AlarmManager alm = (AlarmManager) 
					context.getSystemService(Context.ALARM_SERVICE);
			alm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 
					1000, AikumaSettings.SYNC_INTERVAL, pIntent);
        }
		
	}

}
