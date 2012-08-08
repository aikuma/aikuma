package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.util.Log;

/**
 * The main menu activity that is first run when the application is started.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainActivity extends Activity {
	/**
	 * Initialization when the activity is starting.
	 *
	 * @param	savedInstanceState	Bundle that contains the data most recently
	 * supplied to onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	/**
	 * Load the activity that allows one to record audio.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToRecord(View view) {
		Intent intent = new Intent(this, RecordActivity.class);
		startActivity(intent);
	}

	/**
	 * Load the activity that allows one to select a user.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToUserSelection(View view){
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
	}

	/**
	 * Load the activity that allows one to respeak audio.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToRespeak(View view) {
		//Intent intent = new Intent(this, RespeakActivity.class);
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		startActivity(intent);
	}
}
