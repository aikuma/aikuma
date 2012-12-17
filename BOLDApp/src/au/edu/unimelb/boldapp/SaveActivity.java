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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that offers the user the ability to enter text in a text box and
 * press a button that then subsequently creates a corresponding user.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 *
 */
public class SaveActivity extends Activity {

	static final int SELECT_LANGUAGE = 0;

	/**
	 * The language to save the recording as
	 */
	private Language language;

	/**
	 * UUID of the file being saved.
	 */
	private UUID uuid;

	/**
	 * The UUID of the original audio if the file to be saved is a respeaking;
	 * null if the file to be save isn't a respeaking.
	 */
	private UUID originalUUID;

	/**
	 * Initializes when the activity is started.
	 *
	 * @param	savedInstanceState	Bundle containing data most recently
	 * supplied to onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save);
		setLanguageButton(new Language("English", "eng"));
		Intent intent = getIntent();
		if (intent.getExtras().containsKey("originalUUID")) {
			originalUUID = (UUID) intent.getExtras().get("originalUUID");
			String originalName = intent.getStringExtra("originalName");
			EditText recordingNameEditText = (EditText)
					findViewById(R.id.edit_recording_name);
			Log.i("issue1", recordingNameEditText + " ");
			Log.i("issue1", originalName + " ");
			recordingNameEditText.setText(
					"Respeaking of " + originalName,
					TextView.BufferType.EDITABLE);
		}
		uuid = (UUID) intent.getExtras().get("UUID");
	}

	/**
	 * Takes the user to the language filter to choose the language
	 */
	public void goToLanguageFilter(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	private void setLanguageButton(Language language) {
		Button languageButton = (Button)
				findViewById(R.id.language_button);
		languageButton.setText(language.toString());
	}

	/**
	 *
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == SELECT_LANGUAGE) {
			if (resultCode == RESULT_OK) {
				language = intent.getParcelableExtra("language");
				setLanguageButton(language);
				Log.i("selectLanguage", " " + resultCode);
			}
		}
	}

	/**
	 * Creates a new user with the name specified in the EditText view.
	 *
	 * The user is given a UUID and the user data is written to file.
	 *
	 * @param	view	The create user button that was clicked.
	 */
	public void save(View view) {
		//Get the username from the EditText view.
		EditText editText = (EditText) findViewById(R.id.edit_recording_name);
		String recordingName = editText.getText().toString();

		User currentUser = GlobalState.getCurrentUser();
		Recording recording;
		if (originalUUID == null) {
			recording = new Recording(uuid, currentUser.getUUID(),
					recordingName, new Date(), language);
		} else {
			recording = new Recording(uuid, currentUser.getUUID(),
					recordingName, new Date(), language, originalUUID);
		}

		try {
			FileIO.writeRecording(recording);
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
