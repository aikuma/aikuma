package au.edu.unimelb.boldapp.sync;

import au.edu.unimelb.boldapp.FileIO;

import android.content.Context;
import android.widget.Toast;

/**
 * Offers the syncing capabilities for Activities.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SyncForActivity {
	public static boolean sync(Context context) {
		Client client = new Client();
		client.setClientBaseDir(FileIO.getAppRootPath());
		// Eventually want a method in Client to find it's own server base dir
		// by recursively searching for a writable directory.
		client.setServerBaseDir("/part0/share/bold/");

		if (!client.login("192.168.1.1", "admin", "admin")) {
			Toast.makeText(context, "login failed.", Toast.LENGTH_LONG).show();
			return false;
		} else if (!client.sync()) {
			Toast.makeText(context, "Sync failed.", Toast.LENGTH_LONG).show();
			return false;
		} else if (!client.logout()) {
			Toast.makeText(context, "Logout failed.",
					Toast.LENGTH_LONG).show();
			return false;
		} else {
			Toast.makeText(
					context, "Syncing complete .", Toast.LENGTH_LONG).show();
			return true;
		}
	}
}
