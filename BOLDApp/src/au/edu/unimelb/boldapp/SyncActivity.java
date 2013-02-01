package au.edu.unimelb.boldapp;

import android.util.Log;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import au.edu.unimelb.boldapp.sync.Client;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class SyncActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);
	}

	public void onResume() {
		super.onResume();

		// Load previously set router info.
		try {
			Server server = FileIO.readServer();

			EditText editText = (EditText) findViewById(R.id.edit_ip);
			editText.setText(server.getIPAddress());
			editText = (EditText) findViewById(R.id.edit_router_username);
			editText.setText(server.getUsername());
			editText = (EditText) findViewById(R.id.edit_password);
			editText.setText(server.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/**
	 * When the back button is pressed.
	 *
	 * @param	view	the back button.
	 */
	public void goBack(View view) {
		this.finish();
	}

	/**
	 * When the sync button is pressed, sync with the splash screen.
	 *
	 * @param	view	the sync button.
	 */
	public void sync(View view) {
		// Get the information from the edit views and add to the intent
		EditText editText = (EditText) findViewById(R.id.edit_ip);
		String ipAddress = editText.getText().toString();
		editText = (EditText) findViewById(R.id.edit_router_username);
		String username = editText.getText().toString();
		editText = (EditText) findViewById(R.id.edit_password);
		String password = editText.getText().toString();

		Server server = new Server(ipAddress, username, password);

		if (ipAddress.equals("")) {
			Toast.makeText(this, "Please enter a router IP address",
					Toast.LENGTH_LONG).show();
		} else {
			try {
				FileIO.writeServer(server);
			} catch (IOException e) {
				//Something bad happened, but no big deal, the user will just
				//have to re-enter the credentials later.
			}
			Intent intent = new Intent(this, SyncSplashActivity.class);
			intent.putExtra("ServerInfo", server);
			startActivity(intent);
		}
	}
}
