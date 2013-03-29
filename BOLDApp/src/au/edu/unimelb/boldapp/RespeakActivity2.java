package au.edu.unimelb.aikuma; 
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;

import au.edu.unimelb.aikuma.audio.ThumbRespeaker;

import au.edu.unimelb.aikuma.sensors.ProximityDetector;

import au.edu.unimelb.aikuma.audio.Player;

/**
 * The activity that allows one to respeak audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RespeakActivity2 extends Activity {

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
	protected ThumbRespeaker respeaker;

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
		setContentView(R.layout.respeak2);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new ThumbRespeaker();

		this.uuid = UUID.randomUUID();

		respeaker.prepare(
				new File(FileIO.getRecordingsPath(), originalUUID.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map").toString());

		installBehaviour(savedInstanceState);

		respeaker.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				Log.i("brazil", "onCompletionListener called.");
				ImageButton respeakButton = (ImageButton) 
						findViewById(R.id.RespeakButton);
				ImageButton playButton = (ImageButton) 
						findViewById(R.id.PlayButton);
				playButton.setVisibility(View.INVISIBLE);
				respeaker.setFinishedPlaying(true);
				//respeaker.listenAfterFinishedPlaying();
			}
		});

	}

	private void installBehaviour(Bundle savedInstanceState) {
		final ImageButton respeakButton = (ImageButton)
				findViewById(R.id.RespeakButton);
		final ImageButton playButton = (ImageButton)
				findViewById(R.id.PlayButton);

		playButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!respeaker.getFinishedPlaying()) {
						respeaker.playOriginal();
					}
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					respeaker.pauseOriginal();
				}
				return false;
			}
		});

		respeakButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					respeaker.recordRespeaking();
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					respeaker.pauseRespeaking();
				}
				return false;
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

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
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
		RespeakActivity2.this.finish();
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "RespeakActivity2");
		startActivity(intent);
	}

	public void goToSaveActivity(View view) {
		respeaker.stop();
		Intent intent = new Intent(this, SaveActivity.class);
		intent.putExtra("UUID", uuid);
		intent.putExtra("originalUUID", original.getUUID());
		intent.putExtra("originalName", original.getName());
		startActivity(intent);
		this.finish();
	}

}
