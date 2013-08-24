package org.lp20.aikuma.util;

import android.util.Log;
import java.io.IOException;
import org.lp20.aikuma.model.ServerCredentials;

public class SyncUtil {

	public SyncUtil() throws IOException {
		serverCredentials = ServerCredentials.read();
		syncThread = new SyncLoop();
		syncThread.start();
	}

	private class SyncLoop extends Thread {
		public void run() {
			Log.i("sync", "hi");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			Log.i("sync", "bye");
		}
	}

	private ServerCredentials serverCredentials;
	private Thread syncThread;
}
