package au.edu.unimelb.boldapp;

import java.io.StringWriter;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
public class CreateUserActivity extends Activity {
	/**
	 * Initialization when the activity starts.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_user);
	}

	/**
	 * Creates a new user with the name specified in the EditText view.
	 *
	 * The user is given a UUID and a JSON metadata file containing the users
	 * name is put in users/<uuid>/
	 *
	 * @param	view	The create user button that was clicked.
	 */
	public void createUser(View view) {
		// Get the username from the EditText view.
		EditText editText = (EditText) findViewById(R.id.edit_username);
		String username = editText.getText().toString();

		// Ensure a nonempty string has been entered
		if (username.isEmpty()) {
			Toast.makeText(this,
					"Please enter a username", Toast.LENGTH_LONG).show();
		} else {
			// Generate the uuid that is associated with the user
			UUID uuid = UUID.randomUUID();

			// Create the JSON object
			JSONObject obj = new JSONObject();
			obj.put("name", username);
			obj.put("uuid", uuid.toString());

			// Write the JSON object the the file
			StringWriter stringWriter = new StringWriter();
			try {
				obj.writeJSONString(stringWriter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String jsonText = stringWriter.toString();
			FileIO.write("users/" +
					uuid.toString() + "/metadata.json", jsonText);

			// Return to the user selection activity.
			Intent intent = new Intent(this, UserSelectionActivity.class);
			startActivity(intent);
			this.finish();
		}
	}

	/**
	 * Go back to the user selection activity.
	 * @param	view	the back button.
	 */
	public void goBack(View view) {
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
		this.finish();
	}

}
