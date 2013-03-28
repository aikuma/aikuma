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
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import au.edu.unimelb.aikuma.audio.Audio;
import au.edu.unimelb.aikuma.audio.Respeaker;

import au.edu.unimelb.aikuma.sensors.ProximityDetector;
import au.edu.unimelb.aikuma.audio.analyzer.BackgroundNoise;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;

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
	 * Proximity detector.
	 */
	protected ProximityDetector proximityDetector;

	/**
	 * Sensitivity Slider
	 */
	protected SeekBar sensitivitySlider;

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
		setContentView(R.layout.respeak);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new Respeaker(new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(32768/60, 32768/60)), false);

		this.uuid = UUID.randomUUID();

    BackgroundNoise analyzers = new BackgroundNoise(50);
    int threshold = analyzers.getThreshold();
		Toast.makeText(this, "Threshold: " + threshold, Toast.LENGTH_SHORT).show();

		this.sensitivitySlider = (SeekBar)
				findViewById(R.id.SensitivitySlider);
    this.sensitivitySlider.setMax(threshold*2);
		sensitivitySlider.setOnSeekBarChangeListener(
			new OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar sensitivitySlider,
						int sensitivity, boolean fromUser) {
					Log.i("issue35", "sensitivity: " + sensitivity);
					if (sensitivity == 0) {
						respeaker.setSensitivity(1);
					} else {
						respeaker.setSensitivity(sensitivity);
					}
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		sensitivitySlider.setProgress(threshold);
    
    respeaker.setSensitivity(threshold);
    
		respeaker.prepare(
				new File(FileIO.getRecordingsPath(), originalUUID.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".wav").toString(),
				new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map").toString());

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
		Log.i("RespeakActivity", "onStop");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("RespeakActivity", "onpause");
		this.proximityDetector.stop();
		Audio.reset(this); 
	}

	@Override
	public void onResume() {
		super.onResume();
    
		Audio.playThroughEarpiece(this, false);
		//respeaker.playThroughEarpiece();

		this.proximityDetector =
				new ProximityDetector( RespeakActivity.this, 2.0f) {
					public void near(float distance) {
							respeak();
					}
					public void far(float distance) {
							pause();
					}
				};
		this.proximityDetector.start();
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
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "RespeakActivity");
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
			if (startedRespeaking) {
				respeaker.resume();
			} else {
				startedRespeaking = true;
				respeaker.listen();
			}
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
