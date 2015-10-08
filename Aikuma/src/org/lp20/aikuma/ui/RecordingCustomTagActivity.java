/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Recording.TagType;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * @author 	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingCustomTagActivity extends AikumaActivity {

	private static final String TAG = RecordingCustomTagActivity.class.getCanonicalName();

	private EditText customTagField;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_custom_tag);
		
		// EditText for the custom tag
		customTagField = (EditText) findViewById(R.id.recordingCustomTag);
		customTagField.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if((event.getAction() == KeyEvent.ACTION_DOWN && 
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					onOkButtonPressed(null);
					return true;
				}
				return false;
			}
			
		});
		customTagField.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				int strLen = s.length();
				if(strLen != 0) {
					char c = s.charAt(strLen - 1);
					if(!(Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
						s.delete(strLen - 1, strLen);
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}	
		});
		

		if(savedInstanceState != null) {
			customTagField.setText(savedInstanceState.getString("customTagStr"));
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		String customTagStr = customTagField.getText().toString();
		savedInstanceState.putString("customTagStr", customTagStr);
		
		//Call the superclass to save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Called when the user has confirmed the tag
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		String recordingId = getIntent().getStringExtra("recordingId");
		String customTagStr = customTagField.getText().toString();
		
		ArrayList<String> tagVerIdList = new ArrayList<String>();
		Recording recording;
		try {
			recording = Recording.read(recordingId);
			String tagFileName = recording.tag(TagType.CUSTOM, 
					customTagStr, AikumaSettings.getCurrentUserId());
			if(tagFileName != null)
				tagVerIdList.add(recording.getVersionName() + "-" + tagFileName);
		} catch (IOException e) {
			Log.e(TAG, "The recording can't be tagged: " + e.getMessage());
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
					Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			Toast.makeText(this, "The recording can't be tagged", Toast.LENGTH_LONG).show();
		}
		
		// If automatic-backup is enabled, archive this file
		if(AikumaSettings.isBackupEnabled) {
			Intent serviceIntent = new Intent(RecordingCustomTagActivity.this, 
					GoogleCloudService.class);
			serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "tag");
			serviceIntent.putStringArrayListExtra(GoogleCloudService.ACTION_EXTRA, tagVerIdList);
			serviceIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
					AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
					AikumaSettings.getCurrentUserToken());
			
			startService(serviceIntent);
		}			
		
		int startActivity = getIntent().getIntExtra("start", 0);
		Intent intent;
		if(startActivity == 0) {
			intent = new Intent(this, ListenActivity.class);
		} else {
			intent = new Intent(this, ListenRespeakingActivity.class);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		
		Toast.makeText(this, "Custom tag added", Toast.LENGTH_LONG).show();
	}

}