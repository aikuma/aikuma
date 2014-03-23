/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.audio.record.PhoneRespeaker;
import org.lp20.aikuma.audio.record.analyzers.ThresholdSpeechAnalyzer;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.recognizers.AverageRecognizer;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

/**
 * The Activity that allows users to create respeakings where the control of
 * the original and respeaking is governed by the speaking of the user.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class PhoneRespeakActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_respeak);

		//Lets a method in AikumaActivity superclass know to ask user if they
		//are not willing to discard new data on an activity transition via the
		//menu.
		safeActivityTransition = true;
		fragment = (PhoneRespeakFragment)
				getFragmentManager().findFragmentById(R.id.PhoneRespeakFragment);
		setUpPhoneRespeaker();
		fragment.setPhoneRespeaker(respeaker);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		respeaker.release();
	}

	// Prepares the respeaker.
	private void setUpPhoneRespeaker() {
		Intent intent = getIntent();
		id = (String) intent.getExtras().get("id");
		sampleRate = (Long) intent.getExtras().get("sampleRate");
		respeakingUUID = UUID.randomUUID();
		try {
			originalRecording = Recording.read(id);
			//The threshold speech analyzer here should be automatically
			//detected using Florian's method.
			respeaker = new PhoneRespeaker(originalRecording, respeakingUUID,
					new ThresholdSpeechAnalyzer(88,3,
							new AverageRecognizer(7000,7000)));
		} catch (IOException e) {
			PhoneRespeakActivity.this.finish();
		} catch (MicException e) {
			PhoneRespeakActivity.this.finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		sensitivitySlider = (SeekBar) findViewById(R.id.SensitivitySlider);

		sensitivitySlider.setMax(SettingsActivity.DEFAULT_DEFAULT_SENSITIVITY*2);
		//extractBackgroundNoiseThreshold();
		try {
			setSensitivity(FileIO.readDefaultSensitivity());
		} catch (IOException e) {
			setSensitivity(SettingsActivity.DEFAULT_DEFAULT_SENSITIVITY);
		}

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
	}	

	/**
	 * Sends the appropriate metadata to the RecordingMetadataActivity so that
	 * the data can be saved.
	 *
	 * @param	view	The save respeaking button.
	 */
	public void onSaveRespeakingButton(View view) {
		respeaker.stop();
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		intent.putExtra("uuidString", respeakingUUID.toString());
		intent.putExtra("sampleRate", originalRecording.getSampleRate());
		intent.putExtra("groupId",
				Recording.getGroupIdFromId(id));
		intent.putExtra("durationMsec", respeaker.getCurrentMsec());
		startActivity(intent);
		PhoneRespeakActivity.this.finish();
	}

	/**
	 * Returns the sample rate of the original (and respeaking).
	 *
	 * @return	The sample rate of the original (and respeaking).
	 */
	public long getSampleRate() {
		return this.sampleRate;
	}

	// Currently unused method to automatically detect the background noise and
	// set the sensitivity accordingly.
	/*
	private void extractBackgroundNoiseThreshold() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				try {
					new NoiseLevel(PhoneRespeakActivity.this, 18).find();
				} catch (MicException e) {
					throw new RuntimeException("MicException thrown on detecting noise threshhold");
				}
			}
		}, 500);
	}
	*/

	/**
	 * Sets the threshold which determines whether sound should be considered
	 * speech or not.
	 *
	 * @param	threshold	the threshold.
	 */
	public void setSensitivity(int threshold) {
		this.respeaker.setSensitivity(threshold);
		sensitivitySlider.setProgress(threshold);
	}

	private PhoneRespeakFragment fragment;
	private PhoneRespeaker respeaker;
	private String id;
	private UUID respeakingUUID;
	private Recording originalRecording;
	private long sampleRate;
	private SeekBar sensitivitySlider;
}
