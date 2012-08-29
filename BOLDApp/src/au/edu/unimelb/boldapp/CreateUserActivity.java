package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.StringWriter;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.simple.JSONObject;

/**
 * Activity that offers the user the ability to enter text in a text box, take
 * a photo and press a button that then subsequently creates a corresponding
 * user.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 *
 */
public class CreateUserActivity extends Activity {

	/**
	 * The UUID associated with the yet to be created user.
	 */
	private UUID uuid;

	/**
	 * The request code for taking a photo.
	 */
	static final int PHOTO_REQUEST_CODE = 0;

	/**
	 * Called when the activity is starting.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_user);
		// Generate the uuid that is associated with the user
		this.uuid = UUID.randomUUID();
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

			// Create the JSON object
			JSONObject obj = new JSONObject();
			obj.put("name", username);
			obj.put("uuid", this.uuid.toString());

			// Write the JSON object the the file
			StringWriter stringWriter = new StringWriter();
			try {
				obj.writeJSONString(stringWriter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String jsonText = stringWriter.toString();
			FileIO.write(FileIO.getUsersPath() +
					uuid.toString() + "/metadata.json", jsonText);

			this.finish();

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
	 * Take users profile photo
	 */
	public void takePhoto(View view) {
		dispatchTakePictureIntent(PHOTO_REQUEST_CODE);
	}

	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String imageFilename = this.uuid.toString() + ".jpg";
		try {
			File image = new File(FileIO.getAppRootPath() +
					FileIO.getImagesPath() + imageFilename);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(image));
		} catch (Exception e) {
			e.printStackTrace();
		}

		startActivityForResult(takePictureIntent, actionCode);
	}

	private void handleSmallCameraPhoto() {
		Bitmap mImageBitmap = BitmapFactory.decodeFile(FileIO.getAppRootPath()
				+ FileIO.getImagesPath() + this.uuid.toString() + ".jpg");
		ImageView userPhoto = (ImageView) findViewById(R.id.UserPhoto);
		userPhoto.setImageBitmap(mImageBitmap);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent _data) {
		if (requestCode == PHOTO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				handleSmallCameraPhoto();
			}
		}
	}

}
