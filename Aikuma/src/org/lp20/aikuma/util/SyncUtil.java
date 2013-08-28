package org.lp20.aikuma.util;

import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import org.lp20.aikuma.model.ServerCredentials;
import org.lp20.sync.FTPSyncUtil;

public class SyncUtil {

	public SyncUtil() throws IOException {
		serverCredentials = ServerCredentials.read();
		syncThread = new SyncLoop();
		syncThread.start();
	}

	private class SyncLoop extends Thread {
		public void run() {
			Client client = new Client();
			client.setClientBaseDir(FileIO.getAppRootPath().toString());
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
		}
	}

	private ServerCredentials serverCredentials;
	private Thread syncThread;
}
