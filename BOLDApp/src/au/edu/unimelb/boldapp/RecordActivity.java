package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Recorder;

import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;

/**
 * The activity that allows one to record audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordActivity extends Activity {
	/**
	 * Indicates whether audio is being recorded
	 */
	private Boolean recording;
	/**
	 * Instance of the recorder class that offers methods to record.
	 */
	private Recorder recorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
		recorder = new Recorder();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		RecordActivity.this.finish();
	}

	/**
	 * Move to the activity that allows one to select users.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToUserSelection(View view){
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
	}

	/**
	 * Start and stop the recording of audio.
	 *
	 * @param	view	The record button that was clicked.
	 */
	public void record(View view) {
		//Toggle the recording Boolean.
		User user = GlobalState.getCurrentUser();
		String name = user.getName();
		Log.i("yoyoyo", "hi");
		UUID uuid = UUID.randomUUID();
		if (!recording) {
			recorder.listen("/mnt/sdcard/bold/recordings/" +
					uuid.toString() + ".wav");
		} else {
			recorder.stop();
		}
		recording = !recording;
		Log.i("yoyoyo", recording.toString());
	}
}
