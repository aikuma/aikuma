package au.edu.unimelb.boldapp;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.widget.Toast;

import au.edu.unimelb.boldapp.sync.Client;

import au.edu.unimelb.boldapp.FileIO;

public class SyncActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sync_screen);

        Handler handler = new Handler();

        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
				Log.i("1234", "here0");
				Client client = new Client();
				client.setClientBaseDir(FileIO.getAppRootPath());
				// Eventually want a method in Client to find it's own server
				// base dir by recursively searching for a writable directory.
				client.setServerBaseDir("/part0/share/bold/");

				Log.i("1234", "here1");
				if (!client.login("192.168.1.1", "admin", "admin")) {
					Log.i("1234", "here2");
					Toast.makeText(SyncActivity.this, "login failed.",
							Toast.LENGTH_LONG).show();
					finish();
				} else if (!client.sync()) {
					Log.i("1234", "here3");
					Toast.makeText(SyncActivity.this, "Sync failed.",
							Toast.LENGTH_LONG).show();
					finish();
				} else if (!client.logout()) {
					Log.i("1234", "here4");
					Toast.makeText(SyncActivity.this, "Logout failed.",
							Toast.LENGTH_LONG).show();
					finish();
				} else {
					
					Log.i("1234", "here");
					Toast.makeText(SyncActivity.this, "Syncing Complete.",
							Toast.LENGTH_LONG).show();
				}

                finish();
                // start the home screen

                Intent intent = new Intent(SyncActivity.this,
						InitialUserSelectionActivity.class);
                SyncActivity.this.startActivity(intent);

            }

        }, 50); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called

    }

}
