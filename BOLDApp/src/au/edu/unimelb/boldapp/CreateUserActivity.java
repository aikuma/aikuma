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
 */
public class CreateUserActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_user);
	}

	/**
	Creates a new user with the name specified in the EditText view.

	The user is given a UUID and a JSON metadata file containing the users name
	is put in users/<uuid>/
	*/
	public void createUser(View view) {
		//Get the username from the EditText view.
		EditText editText = (EditText) findViewById(R.id.edit_message);
		String username = editText.getText().toString();

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
	}

	/**
	 * Go back to the user selection activity.
	 */
	public void goBack(View view) {
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
		this.finish();
	}

}
