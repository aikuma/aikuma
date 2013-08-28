package org.lp20.aikuma.util;

import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.lp20.aikuma.model.ServerCredentials;
//import org.lp20.sync.FTPSyncUtil;

public class SyncUtil {

	public SyncUtil() throws IOException {
		Log.i("sync", "in constructor 1");
		serverCredentials = ServerCredentials.read();
		Log.i("sync", "in constructor 2");
		syncThread = new SyncLoop();
		Log.i("sync", "in constructor 3 ");
		syncThread.start();
		Log.i("sync", "in constructor 4 ");
	}

	private class SyncLoop extends Thread {
		public void run() {
			int waitMins = 0;
			boolean syncResult;
			while (true) {
				try {
					TimeUnit.MINUTES.sleep(waitMins);
				} catch (InterruptedException e) {
					//This shouldn't be happening
					Log.i("sync", "Got an interrupted exception");
				}
				try {
					SyncUtil.this.serverCredentials = ServerCredentials.read();
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
					Log.i("sync", "sync complete.");
				}
				Log.i("sync", "end of conditional block");
				waitMins = 1;
			}
		}
	}

	private ServerCredentials serverCredentials;
	private Thread syncThread;
}
