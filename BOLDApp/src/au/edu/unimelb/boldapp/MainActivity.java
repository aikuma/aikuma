package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * The main menu activity that is first run when the application is started.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainActivity extends Activity {
	/**
	 * Called when the activity is initially created.
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
	 * Things to do when the activity resumes
	 */
	@Override
	public void onResume() {
		super.onResume();
		TextView nameView = (TextView) findViewById(R.id.UserName);
		nameView.setText(GlobalState.getCurrentUser().getName());

		ImageButton userSelection = (ImageButton) findViewById(R.id.UserIcon);
		Bitmap userImage = BitmapFactory.decodeFile(FileIO.getAppRootPath()
				+ FileIO.getImagesPath()
				+ GlobalState.getCurrentUser().getUuid().toString() + ".jpg");
		userSelection.setImageBitmap(userImage);
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
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "RespeakActivity");
		startActivity(intent);
	}

	/**
	 * Load the activity that allows one to Listen to audio.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToListen(View view) {
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "ListenActivity");
		startActivity(intent);
	}

	/**
	 * Load the activity that allows one to translate audio.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToTranslate(View view) {
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "TranslateActivity");
		startActivity(intent);
	}
}
