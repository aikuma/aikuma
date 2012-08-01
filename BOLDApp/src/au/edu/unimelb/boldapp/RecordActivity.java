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
	//private Boolean alreadyStarted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
		//alreadyStarted = false;
		recorder = new Recorder();
		recorder.prepare("/mnt/sdcard/bold/recordings/" +
				//		uuid.toString() + ".wav");
						"test.wav");
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		recorder.stop();
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
		if (!recording) {
			//if (!alreadyStarted) {
				UUID uuid = UUID.randomUUID();
				recorder.listen(/*"/mnt/sdcard/bold/recordings/" +
				//		uuid.toString() + ".wav");
						"test.wav"*/);
				//alreadyStarted = true;
			//} else {
			//	recorder.resume();
			//}
		} else {
			recorder.pause();
		}
		recording = !recording;
	}
}
