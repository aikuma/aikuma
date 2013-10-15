package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.audio.record.PhoneRespeaker;
import org.lp20.aikuma.audio.record.analyzers.ThresholdSpeechAnalyzer;
import org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.audio.record.recognizers.AverageRecognizer;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;

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
		try {
			originalRecording = Recording.read(originalUUID);
			//The threshold speech analyzer here should be automatically
			//detected using Florian's method.
			respeaker = new PhoneRespeaker(originalRecording, respeakingUUID,
					new ThresholdSpeechAnalyzer(88,3,
							new AverageRecognizer(10000,10000)));
		} catch (IOException e) {
			PhoneRespeakActivity.this.finish();
		} catch (MicException e) {
			PhoneRespeakActivity.this.finish();
		}
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

	private PhoneRespeakFragment fragment;
	private PhoneRespeaker respeaker;
	private UUID originalUUID;
	private UUID respeakingUUID;
	private Recording originalRecording;
	private long sampleRate;
}
