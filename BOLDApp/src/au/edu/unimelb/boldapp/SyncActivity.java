package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.sync.Client;

import android.app.Activity;
import au.edu.unimelb.boldapp.FileIO;
import android.os.Bundle;
import android.content.Intent;

public class SyncActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);

		Client client = new Client();
		client.setClientBaseDir(FileIO.getAppRootPath());
		// Eventually want a method in Client to find it's own server base dir
		// by recursively searching for a writable directory.
		client.setServerBaseDir("/part0/share/bold/");

		if (!client.login("192.168.1.1", "admin", "admin")) {
			//Toast.makeText(context, "login failed.", Toast.LENGTH_LONG).show();
			this.finish();
		} else if (!client.sync()) {
			//Toast.makeText(context, "Sync failed.", Toast.LENGTH_LONG).show();
			this.finish();
		} else if (!client.logout()) {
			//Toast.makeText(context, "Logout failed.",
			//		Toast.LENGTH_LONG).show();
			this.finish();
		} else {
			//Toast.makeText(
			//		context, "Syncing complete .", Toast.LENGTH_LONG).show();
			this.finish();
		}
	}
}
