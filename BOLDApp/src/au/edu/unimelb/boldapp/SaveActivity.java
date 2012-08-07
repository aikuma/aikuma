package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.UUID;
import android.util.Log;
import java.io.StringWriter;
import org.json.simple.JSONObject;
import android.content.Intent;

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

		/*
		//Generate the uuid that is associated with the user
		UUID uuid = UUID.randomUUID();

		//Create the JSON object
		JSONObject obj = new JSONObject();
		obj.put("name", username);
		obj.put("uuid", uuid.toString());

		//Write the JSON object the the file
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("users/" + uuid.toString() + "/metadata.json", jsonText);

		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
		this.finish();
		*/

		User currentUser = GlobalState.getCurrentUser();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid());
		obj.put("recording_name", recordingName);
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
