package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;
import java.io.File;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.Recorder;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;

/**
 * The activity that allows audio to be recorded
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordActivity extends AikumaActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		this.uuid = UUID.randomUUID();
		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = true;
		try {
			recorder = new Recorder(new File(Recording.getRecordingsPath(),
					uuid.toString() + ".wav"), sampleRate);
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
							Float time = recorder.getCurrentMsec()/1000f;
							/*
							BigDecimal bd = new
									BigDecimal(recorder.getCurrentMsec()/1000f);
							bd = bd.round(new MathContext(1));
							*/
							timeDisplay.setText(Float.toString(time) + "s");
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
	public void onStop() {
		super.onStop();
		try {
			recorder.pause();
		} catch (MicException e) {
			// Maybe make a recording metadata file that refers to the error so
			// that the audio can be salvaged.
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			recorder.pause();
		} catch (MicException e) {
			// Maybe make a recording metadata file that refers to the error so
			// that the audio can be salvaged.
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onRecordButton(View view) {
		ImageButton recordButton =
				(ImageButton) findViewById(R.id.recordButton);
		LinearLayout pauseAndStopButtons =
				(LinearLayout) findViewById(R.id.pauseAndStopButtons);
		recordButton.setVisibility(View.GONE);
		pauseAndStopButtons.setVisibility(View.VISIBLE);
		recorder.listen();
	}

	public void onPauseButton(View view) {
		ImageButton recordButton =
				(ImageButton) findViewById(R.id.recordButton);
		LinearLayout pauseAndStopButtons =
				(LinearLayout) findViewById(R.id.pauseAndStopButtons);
		recordButton.setVisibility(View.VISIBLE);
		pauseAndStopButtons.setVisibility(View.GONE);
		try {
			recorder.pause();
		} catch (MicException e) {
			// Maybe make a recording metadata file that refers to the error so
			// that the audio can be salvaged.
		}
	}

	public void onStopButton(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		intent.putExtra("uuidString", uuid.toString());
		intent.putExtra("sampleRate", sampleRate);
		startActivity(intent);
		RecordActivity.this.finish();
		try {
			recorder.stop();
		} catch (MicException e) {
			// Maybe make a recording metadata file that refers to the error so
			// that the audio can be salvaged.
		}
	}

	private Recorder recorder;
	private UUID uuid;
	private long sampleRate = 16000l;
	private TextView timeDisplay;
}
