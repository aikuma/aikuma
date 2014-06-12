/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.lp20.aikuma.audio.Beeper;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.Recorder;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;
import org.lp20.aikuma.ui.sensors.LocationDetector;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.ImageUtils;
import org.lp20.aikuma.util.VideoUtils;

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
		this.soundUUID = UUID.randomUUID();
		this.videoUUID = UUID.randomUUID();
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
			recorder = new Recorder(new File(Recording.getNoSyncRecordingsPath(),
					soundUUID.toString() + ".wav"), sampleRate);
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
		recorder.release();
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
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // create a file to save the video
        // (not used because EXTRA_OUTPUT is not working in some phones.)
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  
       
        
        // set the video image quality to high(1)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); 

        startActivityForResult(intent, VIDEO_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent result) {
		if(requestCode == VIDEO_REQUEST_CODE) {
			
			if (resultCode == RESULT_OK) {
				Uri videoOriginalUri = result.getData();
				
				try {
					VideoUtils.makeVideoFileFromUri(this, 
							videoOriginalUri, this.videoUUID);
				} catch (IOException e) {
					Toast.makeText(this, 
							"Failed to write the video-recording to file",
							Toast.LENGTH_LONG).show();
				}
				
				//asdfasdf
				Log.i("uriresult", ""+result.getData());
				getContentResolver().delete(result.getData(), null, null);
			}
		}
		
	}
	
	private void copyFile(Uri srcUri, File destFile) {
		
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
		
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
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
	private UUID videoUUID;
	private long sampleRate = 16000l;
	private TextView timeDisplay;
	private ProximityDetector proximityDetector;
	private Beeper beeper;
}
