package org.lp20.aikuma.util;

import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.lp20.aikuma.model.ServerCredentials;
//import org.lp20.sync.FTPSyncUtil;

public class SyncUtil {

	private SyncUtil() {}

	public static void startSyncLoop() {
		if (syncThread == null || !syncThread.isAlive()) {
			syncThread = new SyncLoop();
			syncThread.start();
		}
	}

	public static void syncNow() {
		Log.i("sync", "syncNow called");
		//When we interrupt the syncThread, we fire up a new syncThread which
		//syncs immediately.
		syncThread.interrupt();
		Log.i("sync", "syncThread.interrupt() call finished");
	}

	private static class SyncLoop extends Thread {
		public SyncLoop() {
		}
		public SyncLoop(String syncMessage) {
			this.syncMessage = syncMessage;
		}
		public void run() {
			int waitMins = 1;
			boolean syncResult;
			while (true) {
				try {
					Log.i("sync", "starting to sleep");
					TimeUnit.MINUTES.sleep(waitMins);
					Log.i("sync", "finishing sleep");
				} catch (InterruptedException e) {
					//This shouldn't be happening
					Log.i("sync", "Got an interrupted exception");
					SyncUtil.syncThread = new SyncLoop("Syncing complete");
					SyncUtil.syncThread.start();
					return;
				}
				try {
					SyncUtil.serverCredentials = ServerCredentials.read();
				} catch (IOException e) {
					//We'll cope and assume the old serverCredentials work.
				}
				//For some reason we get an EPIPE unless we instantiate a new
				//Client at each iteration.
				Client client = new Client();
				client.setClientBaseDir(FileIO.getAppRootPath().toString());
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
					Log.i("sync", "sync complete");
					if (syncMessage != null) {
						Log.i("sync", "syncMessage: " + syncMessage);
						syncMessage = null;
					}
				}
				Log.i("sync", "end of conditional block");
			}
		}
		private String syncMessage;
	}

	private static ServerCredentials serverCredentials;
	private static Thread syncThread;
}
