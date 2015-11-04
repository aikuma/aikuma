/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Recording.TagType;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

/**
 * The activity that allows default languages to be specified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingLanguageActivity extends AikumaListActivity {
	
	private static final String TAG = RecordingLanguageActivity.class.getCanonicalName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_language);
		
		mode = getIntent().getStringExtra("mode");
		preferences = 
				this.getSharedPreferences(AikumaSettings.getCurrentUserId(), MODE_PRIVATE);
		
		if(savedInstanceState == null) {
			// Default languages are initially shown
			defaultLanguages = FileIO.readDefaultLanguages();
			selectedLanguages = new ArrayList<Language>();
			Set<String> langBuffer = new HashSet<String>();
			List<Language> sourceLangBuffer = getIntent().getParcelableArrayListExtra("languages");
			if(mode.equals("tag")) {
				String recordingId = getIntent().getStringExtra("recordingId");
				if(recordingId.endsWith(FileModel.SOURCE_TYPE)) {
					langBuffer = preferences.getStringSet(
							AikumaSettings.SOURCE_LANG_BUFFER_KEY, new HashSet<String>());
				} else {
					langBuffer = preferences.getStringSet(
							AikumaSettings.INTERPRET_LANG_BUFFER_KEY, new HashSet<String>());
				}
			} else if(mode.equals("source")) {
				langBuffer = preferences.getStringSet(
						AikumaSettings.SOURCE_LANG_BUFFER_KEY, new HashSet<String>());
			} else if(sourceLangBuffer != null) {
				selectedLanguages.addAll(sourceLangBuffer);
			} else {
				langBuffer = preferences.getStringSet(
						AikumaSettings.INTERPRET_LANG_BUFFER_KEY, new HashSet<String>());
			}
			
			Map<String, String> langCodeMap = Aikuma.getLanguageCodeMap();
			for(String langCode : langBuffer) {
				String langName = langCodeMap.get(langCode);
				Language bufLang = new Language(langName, langCode);
				if(langName != null && defaultLanguages.contains(bufLang)) {
					selectedLanguages.add(bufLang);
				}
			}
				
			// 8 languages will be shown as default languages when app is installed
			if(defaultLanguages.size() == 0) {
				List<Language> langs = Aikuma.getLanguages();
				List<String> langCodes = 
						Arrays.asList("eng", "fra", "spa", "por", "rus", "cmn", "ara", "hin");
				for(Language lang : langs) {
					if(langCodes.contains(lang.getCode())) {
						defaultLanguages.add(lang);
						//selectedDefaultLanguages.add(lang);
					}
				}
				Collections.sort(defaultLanguages);
			}
			
		} else {
			defaultLanguages = savedInstanceState.getParcelableArrayList("languages");
			selectedLanguages = 
					savedInstanceState.getParcelableArrayList("selectedLanguages");
			//updateSpeakerLanguageView(savedSpeakers);
		}
		
		adapter = new LanguagesArrayAdapter(this, defaultLanguages, 
						selectedLanguages);
		setListAdapter(adapter);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the current activity state
		savedInstanceState.putParcelableArrayList("languages", defaultLanguages);
	    savedInstanceState.putParcelableArrayList("selectedLanguages", selectedLanguages);
	    
	    //Call the superclass to save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Called when a user presses the add ISO language button.
	 *
	 * @param	view	The button
	 */
	public void onAddISOLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	/**
	 * Called when a user presses the add custom language button.
	 *
	 * @param	view	The button
	 */
	public void onAddCustomLanguageButton(View view) {
		Intent intent = new Intent(this, AddCustomLanguageActivity.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent
			intent) {
		if (requestCode == SELECT_LANGUAGE) {
			if (resultCode == RESULT_OK) {
				Language language =
						(Language) intent.getParcelableExtra("language");
				if (!defaultLanguages.contains(language)) {
					defaultLanguages.add(language);
					Collections.sort(defaultLanguages);
					adapter.notifyDataSetChanged();
					selectedLanguages.add(language);
				}
			}
		}
	}
	
	/**
	 * Called when the user has confirmed the recording
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		Intent infoIntent = getIntent();
		
		Intent intent;
		if(mode.equals("tag")) {	// Tagging activity
			String recordingId = infoIntent.getStringExtra("recordingId");
			ArrayList<String> tagVerIdList = new ArrayList<String>();
			Recording recording;
			try {
				recording = Recording.read(recordingId);
				String versionName = recording.getVersionName();
				
				for(Language lang : selectedLanguages) {
					String tagFileName = recording.tag(TagType.LANGUAGE, 
							lang.toTagString(), AikumaSettings.getCurrentUserId());
					if(tagFileName != null)
						tagVerIdList.add(versionName + "-" + tagFileName);
				}
			} catch (IOException e) {
				Log.e(TAG, "The recording can't be tagged: " + e.getMessage());
				intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
						Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				Toast.makeText(this, "The recording can't be tagged", Toast.LENGTH_LONG).show();
			}	
			
			if(recordingId.endsWith(FileModel.SOURCE_TYPE)) {
				saveLanguages(AikumaSettings.SOURCE_LANG_BUFFER_KEY, selectedLanguages);
			} else {
				saveLanguages(AikumaSettings.INTERPRET_LANG_BUFFER_KEY, selectedLanguages);
			}
			
			// If automatic-backup is enabled, archive this file
			if(AikumaSettings.isBackupEnabled) {
				Intent serviceIntent = new Intent(RecordingLanguageActivity.this, 
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
			if(startActivity == 0) {
				intent = new Intent(this, ListenActivity.class);
			} else {
				intent = new Intent(this, ListenRespeakingActivity.class);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
					Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			Toast.makeText(this, "Language tag added", Toast.LENGTH_LONG).show();
			
		} else {					// Language selection for source, interpret
			if (mode.equals("source")) {	// source recordings
				intent = new Intent(this, RecordActivity.class);
				saveLanguages(AikumaSettings.SOURCE_LANG_BUFFER_KEY, selectedLanguages);
			} else {						// derivative recordings
				if (mode.equals("phone")) {	// derivative interface
					intent = new Intent(this, PhoneRespeakActivity.class);
				} else {
					// Lets just default to thumb respeaking
					intent = new Intent(this, ThumbRespeakActivity.class);
				}
				saveLanguages(AikumaSettings.INTERPRET_LANG_BUFFER_KEY, selectedLanguages);
				
				intent.putExtra("respeakingType", infoIntent.getStringExtra("respeakingType"));
				intent.putExtra("sourceId", infoIntent.getStringExtra("sourceId"));
				intent.putExtra("ownerId", infoIntent.getStringExtra("ownerId"));
				intent.putExtra("versionName", infoIntent.getStringExtra("versionName"));
				intent.putExtra("sampleRate", infoIntent.getLongExtra("sampleRate", 0));
				intent.putExtra("rewindAmount", infoIntent.getIntExtra("rewindAmount", 0));
			}
			intent.putExtra("mode", mode);
			
			intent.putParcelableArrayListExtra("languages", selectedLanguages);
			
			startActivity(intent);
		}
	}
	
	private void saveLanguages(String key, List<Language> langs) {
		Set<String> langCodeSet = new HashSet<String>();
		Map<String, String> iso639CodeMap = Aikuma.getLanguageCodeMap();
		for(Language lang : langs) {
			String code = lang.getCode();
			if(iso639CodeMap.containsKey(code))
			langCodeSet.add(code);
		}
		Editor editor = preferences.edit();
		editor.putStringSet(key, langCodeSet);
		editor.commit();
	}

	private String mode;
	
	private SharedPreferences preferences;
	
	static final int SELECT_LANGUAGE = 0;
	// Languages shown in the list, and selected by the user.
	private ArrayList<Language> defaultLanguages;
	private ArrayList<Language> selectedLanguages;
	ArrayAdapter<Language> adapter;
}
