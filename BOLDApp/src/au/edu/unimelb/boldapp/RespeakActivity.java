package au.edu.unimelb.boldapp;

import java.util.UUID;
import java.io.StringWriter;
import java.util.Date;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.simple.JSONObject;

import au.edu.unimelb.boldapp.audio.Respeaker;

/**
 * The activity that allows one to respeak audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RespeakActivity extends Activity {

	/**
	 * The prefix of the name of the recording
	 */
	protected String recordingNamePrefix;

	/**
	 * Indicates whether the respeaking has been started already
	 */
	protected Boolean startedRespeaking;

	/**
	 * Indicates whether audio is being recorded
	 */
	 protected Boolean respeaking;

	/**
	 * The recording that is being respoken
	 */
	protected Recording original;

	/**
	 * The UUID of the respeaking;
	 */
	protected UUID uuid;

	/**
	 * Instance of the respeaker class that offers methods to respeak
	 */
	protected Respeaker respeaker;

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

		this.recordingNamePrefix = "Respeaking of ";

		//Get the original from the intent
		Intent intent = getIntent();
		UUID originalUUID = (UUID) intent.getExtras().get("recordingUUID");
		this.original = GlobalState.getRecordingMap().get(originalUUID);
		setContentView(R.layout.respeak);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new Respeaker();

		this.uuid = UUID.randomUUID();

		respeaker.prepare(
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ originalUUID.toString() + ".wav",
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ uuid.toString() + ".wav",
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ uuid.toString() + ".map");

		respeaker.player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton respeakButton = (ImageButton) 
						findViewById(R.id.Respeak);
				ImageButton pauseButton = (ImageButton) 
						findViewById(R.id.Pause);
				pauseButton.setVisibility(View.INVISIBLE);
				respeakButton.setVisibility(View.INVISIBLE);
				//respeaker.stop();
				respeaker.setFinishedPlaying(true);
				respeaker.listenAfterFinishedPlaying();
			}
		});
	}

	/**
	 * Called when the activity goes completely out of view
	 */
	@Override
	public void onStop() {
		//recorder.stop();
		super.onStop();
	}

	/**
	 * Cancel 
	 *
	 * @param	view	The button that was pressed
	 */
	public void cancel(View view){
		respeaker.stop();
		FileIO.delete(FileIO.getRecordingsPath() + uuid.toString() + ".wav");
		FileIO.delete(FileIO.getRecordingsPath() + uuid.toString() + ".map");
		RespeakActivity.this.finish();
	}

	/**
	 * Save the respeaking to file
	 *
	 * @param	view	The button that was clicked.
	 */
	public void save(View view) {
		respeaker.stop();

		//Get a standardized representation of the current date
		String dateString = new StandardDateFormat().format(new Date());

		//Generate metadata file for the recording.
		User currentUser = GlobalState.getCurrentUser();
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid.toString());
		obj.put("creatorUUID", currentUser.getUuid().toString());
		obj.put("originalUUID", original.getUuid().toString());
		obj.put("recording_name", this.recordingNamePrefix +
				original.getName());
		obj.put("date_string", dateString);
		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String jsonText = stringWriter.toString();
		FileIO.write(FileIO.getRecordingsPath() + uuid.toString() + ".json",
				jsonText);
		Toast.makeText(this,
				this.recordingNamePrefix + original.getName() + " saved",
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
