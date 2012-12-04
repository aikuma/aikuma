package au.edu.unimelb.boldapp;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;


import org.json.simple.JSONObject;

/**
 * Activity that offers the user the ability to enter text in a text box and
 * press a button that then subsequently creates a corresponding user.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 *
 */
public class SaveActivity extends Activity {

	/**
	 * UUID of the file being saved.
	 */
	private UUID uuid;

	/**
	 * Initializes when the activity is started.
	 *
	 * @param	savedInstanceState	Bundle containing data most recently
	 * supplied to onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		uuid = (UUID) intent.getExtras().get("UUID");
		setContentView(R.layout.save);
	}

	/**
	 * Creates a new user with the name specified in the EditText view.
	 *
	 * The user is given a UUID and a JSON metadata file containing the users
	 * name is put in users/<uuid>/
	 *
	 * @param	view	The create user button that was clicked.
	 */
	public void save(View view) {
		//Get the username from the EditText view.
		EditText editText = (EditText) findViewById(R.id.edit_recording_name);
		String recordingName = editText.getText().toString();

		User currentUser = GlobalState.getCurrentUser();
		Recording recording = new Recording(uuid, currentUser.getUUID(),
				recordingName, new Date());

		try {
			FileIO.writeRecordingMeta(recording);
			Toast.makeText(this, recordingName + " saved",
					Toast.LENGTH_LONG).show();
			this.finish();
		} catch (IOException e) {
			Toast.makeText(this, "Failed writing " + recordingName,
					Toast.LENGTH_LONG).show();
		}

		this.finish();
	}

	/**
	 * Go back to the record Activity
	 * @param	view	the button pressed.
	 */
	/*
	public void back(View view) {
		this.finish();
	}
	*/

}
