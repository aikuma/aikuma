package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Recorder;

import java.util.UUID;
import java.io.StringWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;

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
	//private Boolean alreadyStarted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
		//alreadyStarted = false;
		recorder = new Recorder();
		User currentUser = GlobalState.getCurrentUser();
		UUID uuid = UUID.randomUUID();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid());
		obj.put("creatorName", currentUser.getName());
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("recordings/" + uuid.toString() + ".json", jsonText);
		recorder.prepare("/mnt/sdcard/bold/recordings/" +
				uuid.toString() + ".wav");
	}

	@Override
	public void onStop() {
		recorder.stop();
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
		recording = !recording;
		if (recording) {
			recorder.listen();
		} else {
			recorder.pause();
		}
	}
}
