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
			FTPSyncUtil ftpSyncUtil = new FTPSyncUtil();
			//client.setClientBaseDir(FileIO.getAppRootPath().toString());
			try {
				ftpSyncUtil.connect("192.168.1.1");
				ftpSyncUtil.login("admin", "admin");
				ftpSyncUtil.setClientSyncDir(FileIO.getAppRootPath());
				Log.i("sync", "findwritabledir: " +
				ftpSyncUtil.findWritableServerDir(new File("/")));
				ftpSyncUtil.setServerSyncDir(
						ftpSyncUtil.findWritableServerDir(new File("/")));
				Log.i("sync", "serverSyncDir: " +
						ftpSyncUtil.getServerSyncDir());
				Log.i("sync", "sync success: " + ftpSyncUtil.sync());
				ftpSyncUtil.logout();
				ftpSyncUtil.disconnect();
			} catch (IOException e) {
				Log.i("sync", "exception thrown: " + e.getMessage());
			}
			Log.i("sync", "sync complete");
		}
	}

	private ServerCredentials serverCredentials;
	private Thread syncThread;
}
