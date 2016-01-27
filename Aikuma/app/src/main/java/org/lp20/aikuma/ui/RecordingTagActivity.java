package org.lp20.aikuma.ui;

import org.lp20.aikuma2.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Recoridng tagging activity
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingTagActivity extends AikumaActivity {

	private String recordingId;
	private int startActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_tag);
		
		recordingId = getIntent().getStringExtra("id");
		startActivity = getIntent().getIntExtra("start", 0);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	/**
	 * Button callback function to add language tags
	 * @param v		The button view
	 */
	public void onAddLanguageTagButton(View v) {
		Intent intent = new Intent(this, RecordingLanguageActivity.class);
		intent.putExtra("start", startActivity);
		intent.putExtra("mode", "tag");
		intent.putExtra("recordingId", recordingId);
		startActivity(intent);
	}
	
	/**
	 * Button callback function to add speaker tags
	 * @param v		The button view
	 */
	public void onAddSpeakerTagButton(View v) {
		Intent intent = new Intent(this, RecordingSpeakersActivity.class);
		intent.putExtra("start", startActivity);
		intent.putExtra("recordingId", recordingId);
		startActivity(intent);
	}
	
	/**
	 * Button callback function to add OLAC tags
	 * @param v		The button view
	 */
	public void onAddOLACTagButton(View v) {
		Intent intent = new Intent(this, RecordingOLACActivity.class);
		intent.putExtra("start", startActivity);
		intent.putExtra("recordingId", recordingId);
		startActivity(intent);
	}

	/**
	 * Button callback function to add custom tags
	 * @param v		The button view
	 */
	public void onAddCustomTagButton(View v) {
		// custom ?? RecordingCustomTag Activity
		Intent intent = new Intent(this, RecordingCustomTagActivity.class);
		intent.putExtra("start", startActivity);
		intent.putExtra("mode", "tag");
		intent.putExtra("recordingId", recordingId);
		startActivity(intent);
	}
	
}
