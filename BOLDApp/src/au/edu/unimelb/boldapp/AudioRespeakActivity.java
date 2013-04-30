package au.edu.unimelb.aikuma;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.concurrent.ExecutionException;

import au.edu.unimelb.aikuma.audio.Audio;
import au.edu.unimelb.aikuma.audio.AudioRespeaker;

import au.edu.unimelb.aikuma.sensors.ProximityDetector;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;
import au.edu.unimelb.aikuma.audio.widgets.NoiseLevel;

/**
 * The activity that allows one to respeak audio.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class AudioRespeakActivity extends Activity {

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
	protected AudioRespeaker respeaker;

	/**
	 * Proximity detector.
	 */
	protected ProximityDetector proximityDetector;

	/**
	 * Sensitivity Slider
	 */
	protected SeekBar sensitivitySlider;
	
	public SeekBar getSensitivitySlider() { return this.sensitivitySlider; }
	public AudioRespeaker getRespeaker() { return this.respeaker; }
	
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
    
    setContentView(R.layout.respeak);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new AudioRespeaker(new ThresholdSpeechAnalyzer(88, 3), false);

		this.uuid = UUID.randomUUID();
    
		respeaker.prepare(
				new File(FileIO.getRecordingsPath(), originalUUID.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map").toString());

		respeaker.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton respeakButton = (ImageButton) 
						findViewById(R.id.Respeak);
				ImageButton pauseButton = (ImageButton) 
						findViewById(R.id.Pause);
				pauseButton.setVisibility(View.INVISIBLE);
				respeakButton.setVisibility(View.INVISIBLE);
				respeaker.setFinishedPlaying(true);
				respeaker.listen(); // For the last time.
			}
		});
	}

  protected void extractBackgroundNoiseThreshold() {
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        new NoiseLevel(AudioRespeakActivity.this, 18).find();
      }
    }, 500);
  };
	public void setSensitivity(int level) {
		getSensitivitySlider().setMax(level*2);
		getSensitivitySlider().setProgress(level);
		getRespeaker().setSensitivity(level);
		
		// Now that we have the sensitivity,
		// we can start the proximity detector
		//
		this.proximityDetector.start();
	}

	/**
	 * Called when the activity goes completely out of view
	 */
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.proximityDetector.stop();
		Audio.reset(this); 
	}

	@Override
	public void onResume() {
		super.onResume();
    
		sensitivitySlider = (SeekBar) findViewById(R.id.SensitivitySlider);
		sensitivitySlider.setOnSeekBarChangeListener(
			new OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar sensitivitySlider,
						int sensitivity, boolean fromUser) {
					if (sensitivity == 0) {
						respeaker.setSensitivity(1);
					} else {
						respeaker.setSensitivity(sensitivity);
					}
				}
				public void onStartTrackingTouch(SeekBar seekBar) {}
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
		
		this.proximityDetector =
		new ProximityDetector( AudioRespeakActivity.this, 2.0f) {
			public void near(float distance) {
					respeak();
			}
			public void far(float distance) {
					pause();
			}
		};
		Audio.playThroughEarpiece(this, false);
		
		extractBackgroundNoiseThreshold();
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
		AudioRespeakActivity.this.finish();
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "AudioRespeakActivity");
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

	public void respeak(View view) {
		respeak();
	}

	public void pause(View view) {
		pause();
	}

	/**
	 * Start/resume the respeaking of audio.
	 *
	 * @param	button	The button that was clicked
	 */
	public void respeak() {
		if (!respeaker.getFinishedPlaying()) {
			ImageButton respeakButton = (ImageButton) findViewById(R.id.Respeak);
			respeaking = true;
			ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
			pauseButton.setVisibility(View.VISIBLE);
			respeakButton.setVisibility(View.INVISIBLE);
			respeaker.resume();
		}
	}

	/**
	 * Pause the respeaking
	 *
	 * @param	button	The pause button that was clicked.
	 */
	 public void pause() {
		if (!respeaker.getFinishedPlaying()) {
			ImageButton pauseButton = (ImageButton) findViewById(R.id.Pause);
			respeaking = false;
			ImageButton respeakButton = (ImageButton) findViewById(R.id.Respeak);
			respeakButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.INVISIBLE);
			respeaker.pause();
		}
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
