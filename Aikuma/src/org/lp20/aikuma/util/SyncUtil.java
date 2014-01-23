/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.lp20.aikuma.model.ServerCredentials;
//import org.lp20.sync.FTPSyncUtil;

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
				try {
					SyncUtil.serverCredentials = ServerCredentials.read();
					//For some reason we get an EPIPE unless we instantiate a new
					//Client at each iteration.
					if (forceSync || serverCredentials.getSyncActivated()) {
						forceSync = false;
						Client client = new Client();
						client.setClientBaseDir(
								FileIO.getAppRootPath().toString());
						Log.i("sync", "beginning sync run");
						if (!client.login(serverCredentials.getIPAddress(),
								serverCredentials.getUsername(),
								serverCredentials.getPassword())) {
							Log.i("sync", "login failed: " +
									serverCredentials.getIPAddress());
						} else if (!client.sync()) {
							Log.i("sync", "sync failed.");
						} else if (!client.logout()) {
							Log.i("sync", "Logout failed.");
						} else {
							Log.i("sync", "sync complete.");
						}
						Log.i("sync", "end of conditional block");
						waitMins = 1;
						Log.i("sync", "sync complete");
					} else {
						Log.i("sync", "not syncing");
					}
				} catch (IOException e) {
					Log.i("npe", "ioexception on serverCredentials.read()");
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

	private static ServerCredentials serverCredentials;
	private static Thread syncThread;
}
