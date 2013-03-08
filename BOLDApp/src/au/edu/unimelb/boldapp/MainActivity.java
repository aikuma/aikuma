package au.edu.unimelb.aikuma;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import au.edu.unimelb.aikuma.sync.SyncForActivity;

/**
 * The main menu activity that is first run when the application is started.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainActivity extends Activity {

	private UUID uuid;
  
	/**
	 * Things to do when the activity resumes
	 */
	@Override
	public void onResume() {
		super.onResume();
		setContentView(R.layout.main);
		TextView nameView = (TextView) findViewById(R.id.UserName);
		assert nameView != null;
		assert GlobalState.getCurrentUser() != null;
		nameView.setText(GlobalState.getCurrentUser().getName());

		if (GlobalState.getCurrentUser().getUuid() != this.uuid) {
			this.uuid = GlobalState.getCurrentUser().getUuid();
			ImageButton userSelection = (ImageButton) 
					findViewById(R.id.UserIcon);
			Bitmap userImage = ImageUtils.retrieveFromFile(
					new File(FileIO.getImagesPath(),
					GlobalState.getCurrentUser().getUuid().toString() +
							".small.jpg"));
			if (userImage != null) {
				userSelection.setImageBitmap(userImage);
			} else {
				userSelection.setImageResource(R.drawable.unknown_user);
			}
		}
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
	public void goToInterpret(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		//intent.putExtra("activity", "InterpretActivity");
		startActivity(intent);
	}

	/**
	 * Synchronize phone with FTP server.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToRespeak2(View view) {
		//SyncForActivity.sync(this);
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "RespeakActivity2");
		startActivity(intent);

	}

	public void sync(View view) {
		Server server;
		try {
			server = FileIO.readServer();
		} catch (IOException e) {
			//server = new Server("us1.hostedftp.com",
			//		"stevenbird1@gmail.com", "DMD819");
			server = new Server("192.168.1.1", "admin", "admin");
		}
		Intent intent = new Intent(this, SyncSplashActivity.class);
		intent.putExtra("ServerInfo", server);
		startActivity(intent);
	}
}
