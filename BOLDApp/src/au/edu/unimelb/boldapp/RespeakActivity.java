package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Respeaker;

import java.util.UUID;
import java.io.StringWriter;
import java.util.Date;
import java.text.DateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.simple.JSONObject;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

/**
 * The activity that allows one to respeak audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RespeakActivity extends Activity {

	/**
	 * Indicates whether the respeaking has been started already
	 */
	private Boolean startedRespeaking;

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
		UUID originalUUID = (UUID) intent.getExtras().get("recordingUUID");
		this.original = GlobalState.getRecordingMap().get(originalUUID);
		Log.i("durp", this.original.getName());
		setContentView(R.layout.respeak);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new Respeaker();

		this.uuid = UUID.randomUUID();

		respeaker.prepare(
				"/mnt/sdcard/bold/recordings/" + originalUUID + ".wav",
				//"/mnt/sdcard/bold/recordings/" + this.uuid + ".wav");
				"/mnt/sdcard/bold/recordings/" + uuid.toString() + ".wav");

		respeaker.player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton respeakButton = (ImageButton) 
						findViewById(R.id.Respeak);
				ImageButton pauseButton = (ImageButton) 
						findViewById(R.id.Pause);
				pauseButton.setVisibility(View.INVISIBLE);
				respeakButton.setVisibility(View.INVISIBLE);
				respeaker.stop();
			}
		});
	}

	@Override
	public void onStop() {
		//recorder.stop();
		super.onStop();
	}

	public void goBack(View view){
		respeaker.stop();
		FileIO.delete("recordings/" + uuid.toString() + ".wav");
		RespeakActivity.this.finish();
	}

	/**
	 * Change to the activity that allows the user to save the wave file.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void save(View view) {
		respeaker.stop();

		//Get the date
		String dateTime = DateFormat.getDateTimeInstance().format(
				new Date());

		//Generate metadata file for the recording.
		User currentUser = GlobalState.getCurrentUser();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid().toString());
		obj.put("originalUUID", original.getUuid().toString());
		obj.put("recording_name", "respeak of " + original.getName());
		obj.put("date_time", dateTime);
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			Log.e("CaughtExceptions", e.getMessage());
		}
		String jsonText = stringWriter.toString();
		FileIO.write("recordings/" + uuid.toString() + ".json", jsonText);
		Toast.makeText(this,
				"Respeaking of " + original.getName() + " saved",
				Toast.LENGTH_LONG).show();
		this.finish();
	}

	/**
	 * Start/resume the respeaking of audio.
	 *
	 * @param	button	The button that was clicked
	 */
	public void respeak(View view) {
		ImageButton respeakButton = (ImageButton) view;
		respeaking = true;
		ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
		pauseButton.setVisibility(View.VISIBLE);
		respeakButton.setVisibility(View.INVISIBLE);
		if (startedRespeaking) {
			respeaker.resume();
		} else {
			startedRespeaking = true;
			respeaker.listen();
		}
	}

	/**
	 * Pause the respeaking
	 *
	 * @param	button	The pause button that was clicked.
	 */
	 public void pause(View view) {
	 	ImageButton pauseButton = (ImageButton) view;
		respeaking = false;
		ImageButton respeakButton = (ImageButton) findViewById(R.id.Respeak);
		respeakButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.INVISIBLE);
		respeaker.pause();
	 }

}
