package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Respeaker;

import java.util.UUID;
import java.io.StringWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.simple.JSONObject;

/**
 * The activity that allows one to record audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RespeakActivity extends Activity {

	/**
	 * Indicates whether audio is being recorded
	 */
	 private Boolean respeaking;

	/**
	 * The recording that is being respoken
	 */
	private Recording original;

	/**
	 * The UUID of the respeaking;
	 */
	private UUID uuid;

	/**
	 * Instance of the respeaker class that offers methods to respeak
	 */
	private Respeaker respeaker;

	/**
	 * Indicates whether audio is being recorded
	 */
	//private Boolean recording;
	/**
	 * Instance of the recorder class that offers methods to record.
	 */
	//private Recorder recorder;
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

		//Get the original from the intent
		Intent intent = getIntent();
		UUID originalUUID = (UUID) intent.getExtras().get("originalUUID");
		this.original = GlobalState.getRecordingMap().get(originalUUID);
		Log.i("durp", this.original.getName());
		setContentView(R.layout.respeak);

		respeaking = false;
		respeaker = new Respeaker();

		this.uuid = UUID.randomUUID();

		respeaker.prepare(
				"/mnt/sdcard/bold/recordings/" + originalUUID + ".wav",
				//"/mnt/sdcard/bold/recordings/" + this.uuid + ".wav");
				"/mnt/sdcard/bold/respeakings/" + uuid.toString() + ".wav");
		//recording = false;
		//recorder = new Recorder();

		/*
		// Get Info about the creator and put it in the respective JSON
		// metadata file User currentUser = GlobalState.getCurrentUser();
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

		//Prepare the recorder
		recorder.prepare("/mnt/sdcard/bold/recordings/" +
				uuid.toString() + ".wav");
		*/
	}

	@Override
	public void onStop() {
		//recorder.stop();
		super.onStop();
	}

	public void goBack(View view){
		respeaker.stop();
		FileIO.delete("respeakings/" + uuid.toString() + ".wav");
		RespeakActivity.this.finish();
	}

	/**
	 * Change to the activity that allows the user to save the wave file.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void save(View view) {
		respeaker.stop();
		//Generate metadata file for the recording.
		User currentUser = GlobalState.getCurrentUser();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid().toString());
		obj.put("originalUUID", original.getUuid().toString());
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("respeakings/" + uuid.toString() + ".json", jsonText);
		Toast.makeText(this,
				"Respeaking of " + original.getName() + " saved",
				Toast.LENGTH_LONG).show();
		this.finish();
	}

	/**
	 * Start and stop the respeaking / recording of audio.
	 *
	 * @param	button	The button that was clicked.
	 */
	public void respeak(View view) {
		ImageButton button = (ImageButton) view;
		respeaking = !respeaking;
		if (respeaking) {
			button.setImageResource(R.drawable.button_pause);
			respeaker.listen();
		} else {
			button.setImageResource(R.drawable.button_record);
			respeaker.pause();
		}
	}

	/**
	 * Start and stop the recording of audio.
	 *
	 * @param	button	The record button that was clicked.
	 */
	/*
	public void record(View view) {
		ImageButton button = (ImageButton) view;
		//Toggle the recording Boolean.
		recording = !recording;
		if (recording) {
			button.setImageResource(R.drawable.main_record);
			recorder.listen();
		} else {
			button.setImageResource(R.drawable.button_record);
			recorder.pause();
		}
	}
	*/
}
