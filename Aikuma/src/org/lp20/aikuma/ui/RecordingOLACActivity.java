/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Recording.TagType;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * OLAC tagging activity
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingOLACActivity extends AikumaListActivity {

	private static final String TAG = RecordingOLACActivity.class.getCanonicalName();
	
	private ArrayList<String> selectedOLACTags;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_olac);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		selectedOLACTags = new ArrayList<String>();
		
		String[] olacTagsStr = 
				getResources().getStringArray(R.array.olac_discourse_tags);
		
		this.setListAdapter(new ArrayAdapter<String>(this, 
				R.layout.recording_olac_list_item, R.id.olacTagName, olacTagsStr) {
			
			private LayoutInflater inflater = (LayoutInflater)
					RecordingOLACActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			@Override
			public View getView(int position, View olacTagView, ViewGroup parent) {
				final String olacTagStr = getItem(position);
				
				if(olacTagView == null) {
					olacTagView = (LinearLayout) 
							inflater.inflate(R.layout.recording_olac_list_item, parent, false);
				}

				((TextView)olacTagView.findViewById(R.id.olacTagName)).setText(olacTagStr);
				
				CheckBox tagCheckBox = (CheckBox) olacTagView.findViewById(R.id.olacTagCheckBox);
				tagCheckBox.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						boolean checked = ((CheckBox) view).isChecked();
						if (checked) {
							selectedOLACTags.add(olacTagStr);
							checked = true;
						} else {
							selectedOLACTags.remove(olacTagStr);
							checked = false;
						}
					}
				});
				
				return olacTagView;
			}
			
		});
	}

	/**
	 * Called when the user has confirmed the tag
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		String recordingId = getIntent().getStringExtra("recordingId");
		
		ArrayList<String> tagVerIdList = new ArrayList<String>();
		Recording recording;
		try {
			recording = Recording.read(recordingId);
			String versionName = recording.getVersionName();
			for(String olacTagStr : selectedOLACTags) {
				String tagFileName = recording.tag(TagType.OLAC, 
						"discourse_" + olacTagStr, AikumaSettings.getCurrentUserId());
				if(tagFileName != null)
					tagVerIdList.add(versionName + "-" + tagFileName);
			}
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
			Intent serviceIntent = new Intent(RecordingOLACActivity.this, 
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
		
		Toast.makeText(this, "OLAC tag added", Toast.LENGTH_LONG).show();
	}
	
}
