/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.FileIO;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AddSpeakerActivity2 extends AikumaListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker2);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Intent intent = getIntent();
		name = (String) intent.getExtras().getString("name");
		TextView nameView = (TextView) findViewById(R.id.nameView1);
		nameView.setText("Name: " + name);
		
		//Lets method in superclass(AikumaAcitivity) know 
		//to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = false;
		safeActivityTransitionMessage = 
				"This will discard the new speaker's language information.";
		
		
		if(savedInstanceState != null) {
			languages = savedInstanceState.getParcelableArrayList("languages");
			selectedLanguages = 
					savedInstanceState.getParcelableArrayList("selectedLanguages");
		} else {
			languages = FileIO.readDefaultLanguages();
			// 8 languages will be selected as default languages when app is installed
			if(languages.size() == 0) {
				List<Language> langs = Aikuma.getLanguages();
				List<String> langCodes = 
						Arrays.asList("eng", "fra", "spa", "por", "rus", "cmn", "ara", "hin");
				for(Language lang : langs) {
					if(langCodes.contains(lang.getCode())) {
						languages.add(lang);
					}
				}
				Collections.sort(languages);
				try {
					FileIO.writeDefaultLanguages(languages);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
			selectedLanguages = new ArrayList<Language>();
		}
		
		okButton = (ImageButton) findViewById(R.id.okButton2);
		okButton.setImageResource(R.drawable.ok_disabled_48);
		okButton.setEnabled(false);

		adapter = new LanguagesArrayAdapter(this, languages, selectedLanguages) {
			@Override
			// When checkbox in a listview is checked/unchecked
			public void updateActivityState() {
				updateOkButton();
			}
		};
		setListAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the current activity state
		savedInstanceState.putParcelableArrayList("languages", languages);
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
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent intent) {
		if (resultCode == RESULT_OK) {
			Language language =
					(Language) intent.getParcelableExtra("language");
			if (!languages.contains(language)) {
				languages.add(language);
				selectedLanguages.add(language);
				adapter.notifyDataSetChanged();
			}
			updateOkButton();
		}
	}

	/**
	 * Called when the user is ready to confirm the creation of the speaker.
	 *
	 * @param	view	The OK button.
	 */
	public void onOkButtonPressed(View view) {	
		Intent intent = new Intent(this, AddSpeakerActivity3.class);
		intent.putExtra("origin", getIntent().getExtras().getInt("origin"));
		intent.putExtra("name", name);
		intent.putParcelableArrayListExtra("languages", selectedLanguages);
		startActivity(intent);
	}

	/**
	 * Disables or enables the OK button if at least one language is selected
	 * used by LanguageArrayAdapter each time checkbox is checked
	 */
	private void updateOkButton() {
		if (selectedLanguages.size() > 0) {
			okButton.setImageResource(R.drawable.ok_48);
			okButton.setEnabled(true);
			safeActivityTransition = true;
		} else {
			okButton.setImageResource(R.drawable.ok_disabled_48);
			okButton.setEnabled(false);
			safeActivityTransition = false;
		}
	}
	

	static final int SELECT_LANGUAGE = 0;
	//Speaker-name
	private String name;
	
	private ImageButton okButton;
	
	private ArrayList<Language> languages = new ArrayList<Language>();
	private ArrayList<Language> selectedLanguages;
	private ArrayAdapter<Language> adapter;
	
	private static final String TAG = "AddSpeakerActivity2";
}
