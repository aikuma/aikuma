/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.lp20.aikuma.model.ServerCredentials;
//import org.lp20.sync.FTPSyncUtil;
import org.lp20.aikuma.R;

/**
 * Periodically syncs with to a server specified in a ServerCredentials object
 * using an FTP Client.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SyncUtil {

	/**
	 * Private constructor to ensure the class cannot be instantiated.
	 */
	private SyncUtil() {}

	/**
	 * Starts the thread that attempts a sync every minute.
	 */
	public static void startSyncLoop() {
		if (syncThread == null || !syncThread.isAlive()) {
			syncThread = new SyncLoop(false);
			syncThread.start();
		}
	}

	/**
	 * Forces a sync to occur now.
	 */
	public static void syncNow() {
		//When we interrupt the syncThread, we fire up a new syncThread which
		//syncs immediately.
		syncThread.interrupt();
	}

	/**
	 * A thread that loops and attempts to sync every minute.
	 */
	private static class SyncLoop extends Thread {
		/**
		 * Constructor for the SyncLoop.
		 *
		 * @param	forceSync	if true, ensures a sync will start immediately.
		 */
		public SyncLoop(boolean forceSync) {
			this.forceSync = forceSync;
		}
		/**
		 * Runs the loop that periodically tries to sync. 
		 */
		@Override
		public void run() {
			int waitMins = 1;
			boolean syncResult;
			while (true) {
				previousStatus = "";
				try {
					SyncUtil.serverCredentials = ServerCredentials.read();
					//For some reason we get an EPIPE unless we instantiate a new
					//Client at each iteration.
					if (forceSync || serverCredentials.getSyncActivated()) {
						setSyncFlag();
						forceSync = false;
						Client client = new Client();
						client.setClientBaseDir(
								FileIO.getAppRootPath().toString());
						Log.i("sync", "beginning sync run");
						if (!client.login(serverCredentials.getIPAddress(),
								serverCredentials.getUsername(),
								serverCredentials.getPassword())) {
							unsetSyncFlag(previousStatus + 
									"\nAuthentication failed.\n(Check IP/UserID/Password/FTP-setup)\n");
							Log.i("sync", "login failed: " +
									serverCredentials.getIPAddress());
						} else if (!client.sync()) {
							Log.i("sync", "sync failed.");
							unsetSyncFlag(previousStatus + "\nTransfer failed\n");
						} else if (!client.logout()) {
							Log.i("sync", "Logout failed.");
							unsetSyncFlag("Logout failed.");
						} else {
							Log.i("sync", "sync complete.");
							// Log the success-date in credential file
							serverCredentials.setLastSyncDate(
									new StandardDateFormat().format(new Date()));
							commitServerCredentials(new Date());
							
							unsetSyncFlag("Sync was finished successfully");
						}
						Log.i("sync", "end of conditional block");
						waitMins = 1;
						Log.i("sync", "sync complete");
					} else {
						Log.i("sync", "not syncing");
					}
				} catch (IOException e) {
					Log.i("npe", "ioexception on serverCredentials.read()");
					unsetSyncFlag("IOException on serverCredentials.read()");
					//We'll cope and assume the old serverCredentials work.
				}
				try {
					TimeUnit.MINUTES.sleep(waitMins);
				} catch (InterruptedException e) {
					SyncUtil.syncThread = new SyncLoop(true);
					SyncUtil.syncThread.start();
					return;
				}
			}
		}
		private boolean forceSync;
	}

	/**
	 * Sets the sync settings activity so that SyncUtil knows what activity to
	 * use to update the sync notification views.
	 *
	 * @param	syncSettingsActivity	the Activity whose views are to be
	 * updated by SyncUtil to reflect syncing progress.
	 */
	public static void setSyncSettingsActivity(Activity syncSettingsActivity) {
		SyncUtil.syncSettingsActivity = syncSettingsActivity;
		updateSyncTextView("Not syncing");
	}

	/**
	 * Show the feedback in the SettingActivity
	 * 
	 * @param status	Feedback-text to the user for current sync-status
	 */
	public static void updateSyncTextView(final String status) {
		if (syncSettingsActivity != null) {
			syncSettingsActivity.runOnUiThread(new Runnable() {
				public void run() {
					TextView syncTextView = (TextView)
							syncSettingsActivity.findViewById(R.id.syncTextNotification);
					syncTextView.setText(status);
				}
			});
			previousStatus = status;
		}
	}

	private static void setSyncFlag() {
		syncing = true;
		updateSyncTextView("Authenticating with FTP server...");
	}

	private static void unsetSyncFlag(String status) {
		syncing = false;
		String date = "";
		if(serverCredentials != null)
			date = serverCredentials.getLastSyncDate();
		updateSyncTextView(status + "\n" + 
		"Last success sync: " + date);
	}

	// Writes the server credentials with latest sync-success date
	private static void commitServerCredentials(Date date) throws IOException {
		try {
			serverCredentials.write();
		} catch (IllegalArgumentException e) {
			Log.e("sync", e.getMessage());
		}
	}	
	
	private static ServerCredentials serverCredentials;
	private static Thread syncThread;
	private static Activity syncSettingsActivity;
	private static boolean syncing;
	
	private static String previousStatus;
}
