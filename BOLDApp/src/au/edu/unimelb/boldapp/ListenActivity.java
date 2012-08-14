package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.Player;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class ListenActivity extends Activity {
	/**
	 * The player that is used
	 */
	private Player player;

	/**
	 * The recording that is being played
	 */
	private Recording recording;

	/**
	 * Indicates whether the recording is being played or not
	 */
	private Boolean startedPlaying;

	/**
	 * Initialization when the activity starts.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		startedPlaying = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);

		Intent intent = getIntent();
		UUID recordingUUID = (UUID) intent.getExtras().get("recordingUUID");
		this.recording = GlobalState.getRecordingMap().get(recordingUUID);

		this.player = new Player();
		player.prepare("mnt/sdcard/bold/recordings/" +
				this.recording.getUuid().toString() + ".wav");

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton playButton = (ImageButton) 
						findViewById(R.id.Play);
				ImageButton pauseButton = (ImageButton) 
						findViewById(R.id.Pause);
				pauseButton.setVisibility(View.INVISIBLE);
				playButton.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * When the activity is stopped
	 */
	@Override
	public void onStop() {
		super.onStop();
		player.stop();
	}

	/**
	 * When the back button is pressed
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goBack(View view){
		player.stop();
		ListenActivity.this.finish();
	}

	/**
	 * When the play button is pressed.
	 *
	 * @param	view	The button that was pressed
	 */
	public void play(View view) {
		ImageButton playButton = (ImageButton) view;
		ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
		pauseButton.setVisibility(View.VISIBLE);
		playButton.setVisibility(View.INVISIBLE);
		if (startedPlaying) {
			player.resume();
		} else {
			player.play();
			startedPlaying = true;
		}
	}

	/**
	 * When the pause button is pressed
	 *
	 * @param	view	The button that was pressed
	 */
	public void pause(View view) {
		ImageButton pauseButton = (ImageButton) view;
		ImageButton playButton = (ImageButton) findViewById(R.id.Play);
		playButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.INVISIBLE);
		player.pause();
	}
}
