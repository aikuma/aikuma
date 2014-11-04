/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;
import java.io.File;

import org.lp20.aikuma.audio.Beeper;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.Recorder;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;
import org.lp20.aikuma.ui.sensors.ProximityDetector;

/**
 * The activity that allows audio to be recorded
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.soundUUID = UUID.randomUUID();
		// Disable the stopButton(saveButton) before the recording starts
		ImageButton stopButton = 
				(ImageButton) findViewById(R.id.stopButton);
		stopButton.setImageResource(R.drawable.save48);
		stopButton.setEnabled(false);
		
		// Set up the Beeper that will make beeps when recording starts and
		// pauses
		beeper = new Beeper(this, new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer _) {
					// There is a reason for this conditional. The beeps take
					// time to be played. If the user reaches the far state 
					// before recording actually starts, then recording will 
					// be then be triggered when the display suggests otherwise.
					Log.i("RecordActivity", "in onCompletion, recording:" + recording);
					if (recording) {
						recorder.listen();
						ImageButton recordButton =
								(ImageButton) findViewById(R.id.recordButton);
						ImageButton pauseButton =
								(ImageButton) findViewById(R.id.pauseButton);
						recordButton.setVisibility(View.GONE);
						pauseButton.setVisibility(View.VISIBLE);
					}
				}
			});
		
		try {
			File f = new File(Recording.getNoSyncRecordingsPath(),
					soundUUID.toString() + ".wav");
			recorder = new Recorder(f, sampleRate);
		} catch (MicException e) {
			this.finish();
			Toast.makeText(getApplicationContext(),
					"Error setting up microphone.",
					Toast.LENGTH_LONG).show();
		}
		timeDisplay = (TextView) findViewById(R.id.timeDisplay);
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					timeDisplay.post(new Runnable() {
						public void run() {
							int time = recorder.getCurrentMsec();
							/*
							BigDecimal bd = new
									BigDecimal(recorder.getCurrentMsec()/1000f);
							bd = bd.round(new MathContext(1));
							*/
							//Lets method in superclass know to ask user if
							//they are willing to discard audio if time>250msec
							if(time > 250) {
								safeActivityTransition = true;
								safeActivityTransitionMessage = "Discard Audio?";
							}
								
							timeDisplay.setText(Float.toString(time/1000f) + "s");
						}
					});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(recorder != null) {
			recorder.release();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		pause();
		this.proximityDetector.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.proximityDetector = new ProximityDetector(this) {
			public void near(float distance) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
				params.screenBrightness = 0;
				getWindow().setAttributes(params);
				//record();
			}
			public void far(float distance) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
				params.screenBrightness = 1;
				getWindow().setAttributes(params);
				//pause();
			}
		};
		this.proximityDetector.start();
		
	}

	// Activates recording
	private void record() {
		
		if (!recording) {
			recording = true;
			ImageButton recordButton =
					(ImageButton) findViewById(R.id.recordButton);
			recordButton.setEnabled(false);
			beeper.beepBeep();
		}
	}

	// Pauses the recording.
	private void pause() {
		if (recording) {
			recording = false;
			ImageButton recordButton =
					(ImageButton) findViewById(R.id.recordButton);
			ImageButton pauseButton =
					(ImageButton) findViewById(R.id.pauseButton);
			ImageButton stopButton =
					(ImageButton) findViewById(R.id.stopButton);
			recordButton.setEnabled(true);
			stopButton.setImageResource(R.drawable.save_activate48);
			stopButton.setEnabled(true);
			recordButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.GONE);
			try {
				recorder.pause();
				Beeper.beep(this, null);
			} catch (MicException e) {
				// Maybe make a recording metadata file that refers to the error so
				// that the audio can be salvaged.
			}
		}
	}

	/**
	 * Called when the video-record button is pressed 
	 * - starts the video-recording activiy
	 *
	 * @param	view	The record button
	 */
	public void onVideoRecord(View view) {
		// create new video-recording activity	
		Intent intent = new Intent(this, RecordVideoActivity.class);
		startActivity(intent);
	}

	/**
	 * Called when the record button is pressed - starts recording.
	 *
	 * @param	view	The record button
	 */
	public void onRecordButton(View view) {
		record();
	}

	/**
	 * Called when the pause button is pressed - pauses recording.
	 *
	 * @param	view	The pause button
	 */
	public void onPauseButton(View view) {
		pause();
	}

	/**
	 * Called when the stop/save button is pressed; sends some metadata off to
	 * RecordingMetadataActivity.
	 *
	 * @param	view	The stop/save button.
	 */
	public void onStopButton(View view) {
		int duration = recorder.getCurrentMsec();
		try {
			recorder.stop();
		} catch (MicException e) {
			// Maybe make a recording metadata file that refers to the error so
			// that the audio can be salvaged.
		}
		Double latitude = MainActivity.locationDetector.getLatitude();
		Double longitude = MainActivity.locationDetector.getLongitude();
		
		Intent intent = new Intent(this, RecordingMetadataActivity1.class);
		intent.putExtra("uuidString", soundUUID.toString());
		intent.putExtra("sampleRate", sampleRate);
		Log.i("duration", "RecordActivity end: " + duration);
		intent.putExtra("durationMsec", duration);
		intent.putExtra("numChannels", recorder.getNumChannels());
		intent.putExtra("format", recorder.getFormat());
		intent.putExtra("bitsPerSample", recorder.getBitsPerSample());
		if(latitude != null && longitude != null) {
			// if location data is available, put else don't put
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
		}
		
		startActivity(intent);
		RecordActivity.this.finish();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (proximityDetector.isNear()) {
			return false;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}
	
	static final int VIDEO_REQUEST_CODE = 0;

	private boolean recording;
	private Recorder recorder;
	private UUID soundUUID;
	private long sampleRate = 16000l;
	private TextView timeDisplay;
	private ProximityDetector proximityDetector;
	private Beeper beeper;
}
