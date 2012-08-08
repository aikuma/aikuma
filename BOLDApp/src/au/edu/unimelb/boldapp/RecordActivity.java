package au.edu.unimelb.boldapp;

import java.util.UUID;
import java.io.StringWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;

import org.json.simple.JSONObject;

import au.edu.unimelb.boldapp.audio.Recorder;
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

	/**
	 * UUID of the file being recorded.
	 */
	private UUID uuid;

	/**
	 * Called when the activity starts.
	 *
	 * Generates a UUID for the recording, prepares the file.
	 *
	 * @param	savedInstanceState	Bundle containing data most recently
	 * supplied in onSaveInstanceState(bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
		recorder = new Recorder();

		this.uuid = UUID.randomUUID();

		//Prepare the recorder
		recorder.prepare("/mnt/sdcard/bold/recordings/" +
				uuid.toString() + ".wav");
	}

	/**
	 * Stops the recording when stopped
	 */
	@Override
	public void onStop() {
		recorder.stop();
		super.onStop();
	}

	/**
	 * Returns to the previous activity without saving the wav.
	 *
	 * Delete's the wav file on the way out.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goBack(View view){
		recorder.stop();
		FileIO.delete("recordings/" + uuid.toString() + ".wav");
		RecordActivity.this.finish();
	}

	/**
	 * Change to the activity that allows the user to save the wave file.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToSaveActivity(View view){
		Intent intent = new Intent(this, SaveActivity.class);
		intent.putExtra("UUID", uuid);
		startActivity(intent);
		this.finish();
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
	 * @param	button	The record button that was clicked.
	 */
	public void record(View view) {
		ImageButton button = (ImageButton) view;
		//Toggle the recording Boolean.
		recording = !recording;
		if (recording) {
			button.setImageResource(R.drawable.button_pause);
			recorder.listen();
		} else {
			button.setImageResource(R.drawable.button_record);
			recorder.pause();
		}
	}
}
