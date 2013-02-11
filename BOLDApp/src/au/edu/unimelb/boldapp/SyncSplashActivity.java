package au.edu.unimelb.aikuma;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.widget.Toast;
import au.edu.unimelb.aikuma.sync.Client;
import au.edu.unimelb.aikuma.FileIO;

public class SyncSplashActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sync_splash);

		final Server server = (Server) getIntent().getExtras().get("ServerInfo");

        Handler handler = new Handler();

        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

				Client client = new Client();
				client.setClientBaseDir(FileIO.getAppRootPath().toString());
				// Eventually want a method in Client to find it's own server
				// base dir by recursively searching for a writable directory.
				//client.setServerBaseDir("/part0/share/bold/");
				if (!client.login(server.getIPAddress(), server.getUsername(),
						server.getPassword())) {
					Log.i("1234", "here2");
					Toast.makeText(SyncSplashActivity.this, "Login failed." + 
					" Are you connected to the correct wireless network?",
							Toast.LENGTH_LONG).show();
					finish();
				} else if (!client.sync()) {
					Log.i("1234", "here3");
					Toast.makeText(SyncSplashActivity.this, "Sync failed.",
							Toast.LENGTH_LONG).show();
					finish();
				} else if (!client.logout()) {
					Log.i("1234", "here4");
					Toast.makeText(SyncSplashActivity.this, "Logout failed.",
							Toast.LENGTH_LONG).show();
					finish();
				} else {
					
					Log.i("1234", "here");
					Toast.makeText(SyncSplashActivity.this, "Syncing Complete.",
							Toast.LENGTH_LONG).show();
				}

				finish();
				// start the home screen

				/*
				Intent intent = new Intent(SyncSplashActivity.this,
						InitialUserSelectionActivity.class);
				SyncSplashActivity.this.startActivity(intent);
				*/

            }

        }, 50); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called

    }

}
