package au.edu.unimelb.boldapp;

import android.util.Log;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import au.edu.unimelb.boldapp.sync.Client;

public class SyncActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);
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
		Intent intent = new Intent(this, SyncSplashActivity.class);
		startActivity(intent);
	}

}
