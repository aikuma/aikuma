package au.edu.unimelb.aikuma;

import java.io.StringWriter;
import java.util.UUID;
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import au.edu.unimelb.aikuma.audio.Recorder;
import au.edu.unimelb.aikuma.audio.Player;

import au.edu.unimelb.aikuma.sensors.ProximityDetector;

import android.media.MediaPlayer;

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
	 * Proximity detector.
	 */
	protected ProximityDetector proximityDetector;

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
		recorder = new Recorder(getApplicationContext());

		this.uuid = UUID.randomUUID();

		//Prepare the recorder
		recorder.prepare(new File(FileIO.getRecordingsPath(),
				uuid.toString() + ".wav").toString());

	}

	/**
	 * Stops the recording when stopped
	 */
	@Override
	public void onStop() {
		super.onStop();
		/*
		//Pause the recording and adjust the buttons accordingly.
		pause(findViewById(R.id.Pause));
		*/
		recorder.stop();
	}

	@Override 
	public void onPause() {
		super.onPause();
		this.proximityDetector.stop();
	}

	@Override
	public void onResume() {
		super.onResume();

		this.proximityDetector =
				new ProximityDetector(RecordActivity.this, 2.0f) {
					public void near(float distance) {
						record();
					}
					public void far(float distance) {
						pause();
					}
				};
		this.proximityDetector.start();

	}

	/**
	 * Returns to the previous activity without saving the wav.
	 *
	 * Deletes the wav file on the way out.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void cancel(View view){
		recorder.stop();
		//Delete the file that was just created for the recording.
		FileIO.delete(FileIO.getRecordingsPath() + uuid.toString() + ".wav");
		this.finish();
	}

	/**
	 * Change to the activity that allows the user to save the wave file.
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goToSaveActivity(View view){
		recorder.stop();
		Intent intent = new Intent(this, SaveActivity.class);
		intent.putExtra("UUID", uuid);
		startActivity(intent);
		this.finish();
	}

	public void record(View view) {
		record();
	}

	public void pause(View view) {
		pause();
	}

	/**
	 * Start and stop the recording of audio.
	 *
	 * @param	button	The record button that was clicked.
	 */
	public void record() {
		ImageButton recordButton = (ImageButton) findViewById(R.id.Record);
		//Toggle the recording Boolean.
		recording = true;
		ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
		pauseButton.setVisibility(View.VISIBLE);
		recordButton.setVisibility(View.INVISIBLE);
		recorder.listen();
	}

	/**
	 * Pause the recording of the audio.
	 *
	 * @param	button	The pause button that was clicked.
	 */
	public void pause() {
		ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
		//Toggle the recording Boolean.
		recording = false;
		ImageButton recordButton = (ImageButton) findViewById(R.id.Record);
		recordButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.INVISIBLE);
		recorder.pause();
	}

	/**
	 * If phone is close to the ear, any touch event will be ignored.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (proximityDetector.isNear()) {
			return false;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

}
