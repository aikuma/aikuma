package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Recorder;

import java.util.UUID;
import java.io.StringWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;

import org.json.simple.JSONObject;

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
	//private Boolean alreadyStarted;

	/**
	 * Called when the activity starts.
	 *
	 * Generates a UUID for the recording, prepares the file and creates a
	 * metadata file that includes the name and UUID of the user who made the
	 * recording.
	 *
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
		recorder = new Recorder();

		// Get Info about the creator and put it in the respective JSON
		// metadata file User currentUser = GlobalState.getCurrentUser();
		//User currentUser = GlobalState.getCurrentUser();
		this.uuid = UUID.randomUUID();
		/*
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid());
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("recordings/" + uuid.toString() + ".json", jsonText);
		*/

		//Prepare the recorder
		recorder.prepare("/mnt/sdcard/bold/recordings/" +
				uuid.toString() + ".wav");
	}

	@Override
	public void onStop() {
		recorder.stop();
		super.onStop();
	}

	public void goBack(View view){
		recorder.stop();
		FileIO.delete("recordings/" + uuid.toString() + ".wav");
		FileIO.delete("recordings/" + uuid.toString() + ".json");
		RecordActivity.this.finish();
	}

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
