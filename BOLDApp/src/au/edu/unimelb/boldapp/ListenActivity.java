package au.edu.unimelb.boldapp;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import au.edu.unimelb.boldapp.audio.Player;

/**
 * Activity that allows the user to listen to recordings
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenActivity extends Activity
		implements Runnable, SeekBar.OnSeekBarChangeListener {
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
	 * The progress bar
	 */
	private SeekBar seekBar;

	/**
	 * Thread to deal with updating of progress bar
	 */
	private Thread seekBarThread;

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

		this.seekBar = (SeekBar) findViewById(R.id.SeekBar);
		this.seekBar.setOnSeekBarChangeListener(this);
		this.seekBarThread = new Thread(this);

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton button = (ImageButton) 
						findViewById(R.id.Play);
				player.setPlaying(false);
				startedPlaying = false;
				button.setImageResource(R.drawable.button_play);
				seekBarThread.interrupt();
				seekBar.setProgress(seekBar.getMax());
			}
		});

		Log.i("progress", "yo");

	}

	/**
	 * When the activity is stopped
	 */
	@Override
	public void onStop() {
		super.onStop();
		this.seekBarThread.interrupt();
		player.stop();
	}

	/**
	 * When the back button is pressed
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goBack(View view){
		player.stop();
		this.seekBarThread.interrupt();
		ListenActivity.this.finish();
	}

	/**
	 * When the play button is pressed.
	 *
	 * @param	view	The button that was pressed
	 */
	public void play(View view) {
		ImageButton button = (ImageButton) view;
		//ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
		//pauseButton.setVisibility(View.VISIBLE);
		//playButton.setVisibility(View.INVISIBLE);
		if (!player.isPlaying()) {
			button.setImageResource(R.drawable.button_pause);
			if (startedPlaying) {
				player.resume();
			} else {
				player.play();
				seekBar.setProgress(0);
				this.seekBarThread = new Thread(this);
				this.seekBarThread.start();
				startedPlaying = true;
			}
		} else {
			button.setImageResource(R.drawable.button_play);
			player.pause();
			seekBar.setProgress((int)(((float)player.getCurrentPosition()/
					(float)player.getDuration())*100));
		}
	}

	/**
	 * When the pause button is pressed
	 *
	 * @param	view	The button that was pressed
	 */
	 /*
	public void pause(View view) {
		ImageButton pauseButton = (ImageButton) view;
		ImageButton playButton = (ImageButton) findViewById(R.id.Play);
		playButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.INVISIBLE);
		player.pause();
	}
	*/

	@Override
	public void run() {
		int currentPosition = 0;
		int total = player.getDuration();
		while (/*player!=null && */currentPosition<total) {
			try {
				Thread.sleep(1000);
				if (player == null) {
					currentPosition = total;
				} else {
					currentPosition = player.getCurrentPosition();
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			Log.i("lammbock", seekBar.getMax() + " " +
					(int)(((float)currentPosition/(float)total)*100));
			seekBar.setProgress((int)(((float)currentPosition/(float)total)*100));
		}
	}

	@Override
	public void onProgressChanged(
			SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			Log.i("progress", " " +
					(int)Math.round((((float)progress)/100)*player.getDuration()));
			player.seekTo((int)Math.round((((float)progress)/100)*player.getDuration()));
		} else {
			//Progress was changed programatically
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.i("progress", "start");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.i("progress", "stop");
	}
}
