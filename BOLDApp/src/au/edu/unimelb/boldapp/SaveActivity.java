package au.edu.unimelb.boldapp;

import java.io.StringWriter;
import java.util.UUID;
import java.util.Date;
import java.text.DateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.content.Intent;


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
		Log.i("yoyoyo", uuid.toString());
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

		//Get the date
		String dateTime = DateFormat.getDateTimeInstance().format(
				new Date());

		//Generate metadata file for the recording.
		User currentUser = GlobalState.getCurrentUser();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid().toString());
		obj.put("recording_name", recordingName);
		obj.put("date_time", dateTime);
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("recordings/" + uuid.toString() + ".json", jsonText);
		this.finish();

	}

	/**
	 * Go back to the save activity.
	 * @param	view	the back button.
	 */
	public void goBack(View view) {
		this.finish();
	}

}
