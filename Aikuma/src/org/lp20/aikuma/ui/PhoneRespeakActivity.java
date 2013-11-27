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

/**
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

	private void setUpPhoneRespeaker() {
		Intent intent = getIntent();
		originalUUID = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		respeakingUUID = UUID.randomUUID();
		sampleRate = (Long) intent.getExtras().get("sampleRate");
		try {
			originalRecording = Recording.read(originalUUID);
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
		extractBackgroundNoiseThreshold();
	}	

	public void onSaveRespeakingButton(View view) {
		respeaker.stop();
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		intent.putExtra("uuidString", respeakingUUID.toString());
		intent.putExtra("sampleRate", originalRecording.getSampleRate());
		intent.putExtra("originalUUIDString", originalUUID.toString());
		startActivity(intent);
		PhoneRespeakActivity.this.finish();
	}

	public long getSampleRate() {
		return this.sampleRate;
	}

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

	public void setSensitivity(int level) {
		this.respeaker.setSensitivity(level);
		sensitivitySlider.setMax(level*2);
		sensitivitySlider.setProgress(level);
	}

	private PhoneRespeakFragment fragment;
	private PhoneRespeaker respeaker;
	private UUID originalUUID;
	private UUID respeakingUUID;
	private Recording originalRecording;
	private long sampleRate;
	private SeekBar sensitivitySlider;
}
