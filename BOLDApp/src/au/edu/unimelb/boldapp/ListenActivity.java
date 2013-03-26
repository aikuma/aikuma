package au.edu.unimelb.aikuma;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


import au.edu.unimelb.aikuma.audio.Audio;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.view.View;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import au.edu.unimelb.aikuma.audio.PlayerInterface;
import au.edu.unimelb.aikuma.audio.SimplePlayer;
import au.edu.unimelb.aikuma.audio.MarkedMediaPlayer;
import au.edu.unimelb.aikuma.audio.InterleavedPlayer;
import au.edu.unimelb.aikuma.sensors.ProximityDetector;
import au.edu.unimelb.aikuma.sensors.ShakeDetector;
import java.util.UUID;

/**
 * Activity that allows the user to listen to recordings
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenActivity extends Activity
		implements Runnable, SeekBar.OnSeekBarChangeListener {

	/**
	 * The player that is used.
	 */
	private PlayerInterface player;

	/**
	 * The recording that is being played.
	 */
	private Recording recording;

	/**
	 * Indicates whether the recording has begun playing or not; is reset to
	 * false when playing is complete.
	 */
	private Boolean startedPlaying;

	/**
	 * The progress bar.
	 */
	private SeekBar seekBar;

	/**
	 * Thread to deal with updating of progress bar.
	 */
	private Thread seekBarThread;
  
	/**
	 * Proximity detector to start/stop.
	 */
	protected ProximityDetector proximityDetector;

	/**
	 * Shake detector to rewind.
	 */
	protected ShakeDetector shakeDetector;

  protected File likeFile;

	/**
	 * Initialization when the activity starts.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);

		startedPlaying = false;

		// Set the recording that is to be played.
		Intent intent = getIntent();
		UUID recordingUUID = (UUID) intent.getExtras().get("recordingUUID");
		this.recording = GlobalState.getRecordingMap().get(recordingUUID);

		// Set up the player
		if (this.recording.isOriginal()) {
			this.player = new SimplePlayer(this.recording.getUuid());
		} else {
			if (intent.getBooleanExtra("interleavedChoice", true)) {
				try {
					this.player = new InterleavedPlayer(this.recording.getUuid());
					Log.i("segments", "using interleaved player");
				} catch (Exception e) {
					Toast.makeText(this, 
							e.getMessage(),
							Toast.LENGTH_LONG).show();
					Log.e("ListenActivity", "fine dime brizzle", e);
					ListenActivity.this.finish();
				}
			} else {
				this.player = new SimplePlayer(this.recording.getUuid());
			}
		}

		this.seekBar = (SeekBar) findViewById(R.id.SeekBar);
		this.seekBar.setOnSeekBarChangeListener(this);
		if (!this.recording.isOriginal()) {
			if (intent.getBooleanExtra("interleavedChoice", true)) {
				Log.i("ListenActivity", "interleavedChoice");
				this.seekBar.setVisibility(View.INVISIBLE);
			}
		}

		// The code that is to be run when the player is complete.
		if (player != null) {
			player.setOnCompletionListener(
					new InterleavedPlayerOnCompletionListener());
				/*
				@Override
				public void onCompletion(MediaPlayer _player) {
					Log.i("segments", "on completion hit!");
					// Reset the play button
					ImageButton button = (ImageButton) 
							findViewById(R.id.Play);
					button.setImageResource(R.drawable.button_play);
					// Adjust relevant booleans
					startedPlaying = false;
					// Stop the seekBarThread and set the progress to max.
					if (seekBarThread != null) {
						seekBarThread.interrupt();
					}
					try {
						ListenActivity.this.player =
								new InterleavedPlayer(ListenActivity.this.recording.getUuid());
					} catch (Exception e) {
						//Why is this a pokemon exception handler?... interleaved should
						//not be throwing those.
					}
					seekBar.setProgress(seekBar.getMax());
				}
			});
			*/
		}
	}

	private class InterleavedPlayerOnCompletionListener implements
			OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer _) {
			Log.i("segments", "on completion hit!");
			// Reset the play button
			ImageButton button = (ImageButton) 
					findViewById(R.id.Play);
			button.setImageResource(R.drawable.button_play);
			// Adjust relevant booleans
			startedPlaying = false;
			// Stop the seekBarThread and set the progress to max.
			if (seekBarThread != null) {
				seekBarThread.interrupt();
			}
			try {
				ListenActivity.this.player =
						new InterleavedPlayer(ListenActivity.this.recording.getUuid());
				ListenActivity.this.player.setOnCompletionListener(new
						InterleavedPlayerOnCompletionListener());
			} catch (Exception e) {
				//Why is this a pokemon exception handler?... interleaved should
				//not be throwing those.
			}
			seekBar.setProgress(seekBar.getMax());
		}
	}

	/**
	 * Called when someone "like"s the recording.
	 */
	public void like(View view) {
		ImageButton likeButton = (ImageButton) view;
		try {
			this.likeFile.createNewFile();
		} catch (IOException e) {
			//Do nothing
		}
		//likeButton.setVisibility(View.INVISIBLE);
		likeButton.setEnabled(false);
	}
  
  /**
	 * When the activity is resumed.
	 *
	 * TODO Refactor as soon as the play method is refactored.
	 */
	@Override
	public void onResume() {
		ImageButton likeButton = (ImageButton) findViewById(R.id.Like);
		this.likeFile = new File(FileIO.getRecordingsPath(),
				this.recording.getUUID() + "/likes/" +
				GlobalState.getCurrentUser().getUUID());
		likeFile.getParentFile().mkdirs();
		if (likeFile.exists()) {
			//likeButton.setVisibility(View.INVISIBLE);
			likeButton.setEnabled(false);
		}
		super.onResume();
		
		this.proximityDetector = new ProximityDetector(ListenActivity.this, 2.0f) {
		  public void near(float distance) {
			Audio.playThroughEarpiece(ListenActivity.this, true);
				play();
		  }
		  public void far(float distance) {
			Audio.playThroughSpeaker(ListenActivity.this);
				pause();
		  }
		};
		this.proximityDetector.start();

		this.shakeDetector = new ShakeDetector(ListenActivity.this, 2.0f) {
			public void shaken(float acceleration) {
				player.rewind(3000);
			}
		};

		this.shakeDetector.start();
	}

	/**
	 * When the activity is stopped.
	 */
	@Override
	public void onStop() {
		super.onStop();
		if (this.seekBarThread != null) {
			this.seekBarThread.interrupt();
		}
		player.release();
		this.proximityDetector.stop();
		this.shakeDetector.stop();
		ListenActivity.this.finish();
	}

	/**
	 * When the back button is pressed
	 *
	 * @param	view	The button that was clicked.
	 */
	public void goBack(View view){
		player.release();
		if (this.seekBarThread != null) {
			this.seekBarThread.interrupt();
		}
		ListenActivity.this.finish();
	}

	/**
	 * Play the audio
	 */
	public void play() {
		ImageButton button = (ImageButton) findViewById(R.id.Play);
		button.setImageResource(R.drawable.button_pause);
		player.start();
		if (!startedPlaying) {
			// If the user hasn't changed the progress to some other
			// unfinished point in the recording
			if (seekBar.getProgress() == seekBar.getMax()) {
				seekBar.setProgress(0);
			}
			this.seekBarThread = new Thread(this);
			this.seekBarThread.start();
			startedPlaying = true;
		}
	}

	/**
	 * Pause the audio
	 */
	public void pause() {
		ImageButton button = (ImageButton) findViewById(R.id.Play);
		button.setImageResource(R.drawable.button_play);
		player.pause();
		updateProgress();
	}

	/**
	 * When the play button is pressed.
	 *
	 * @param	view	The button that was pressed
	 */
	public void handlePlayButton(View view) {
		if (!player.isPlaying()) {
			play();
		} else {
			pause();
		}
	}
  
	/**
	 * Updates the seek bar to the current progress.
	 */
	protected void updateProgress() {
		seekBar.setProgress((int)(((float)player.getCurrentPosition()/
				(float)player.getDuration())*100));
	}

	/**
	 * The run function for the thread that updates the seekBar.
	 */
	@Override
	public void run() {
		int currentPosition = 0;
		int total = player.getDuration();
		while (currentPosition<total) {
			try {
				Thread.sleep(1000);
				if (player == null) {
					currentPosition = total;
				} else {
					currentPosition = player.getCurrentPosition();
					//Log.i("segCount", "current pos: " + currentPosition);
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// TODO Replace with updateProgress() – do not use
			// currentPosition, but always player.getCurrentPosition.
			//
			// TODO Why is player == null necessary above when
			// player.getDuration is called before we reach it?
			//
			seekBar.setProgress(
					(int)(((float)currentPosition/(float)total)*100));
		}
	}

	/**
	 * When the seekBar's progress is changed.
	 */
	@Override
	public void onProgressChanged(
			SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			player.seekTo((int)Math.round(
					(((float)progress)/100)*player.getDuration()));
		} else {
			//Progress was changed programmatically
		}
	}

	/**
	* If it is close to the ear, any touch event will
	* be ignored.
	*/
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (proximityDetector.isNear()) {
		  return false;
		} else {
		  return super.dispatchTouchEvent(event);
		}
	}

	/**
	 * Obligated to implement this, but we need no functionality here.
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Obligated to implement this, but we need no functionality here.
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}
