/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.FileIO;

/**
 * The activity that allows default languages to be specified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class DefaultLanguagesActivity extends AikumaListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.default_languages);
		// Default languages are initially selected
		defaultLanguages = FileIO.readDefaultLanguages();
		selectedDefaultLanguages=  FileIO.readDefaultLanguages();
		// 8 languages will be selected as default languages when app is installed
		if(defaultLanguages.size() == 0) {
			List<Language> langs = Aikuma.getLanguages();
			List<String> langCodes = 
					Arrays.asList("eng", "fra", "spa", "por", "rus", "cmn", "ara", "hin");
			for(Language lang : langs) {
				if(langCodes.contains(lang.getCode())) {
					defaultLanguages.add(lang);
					selectedDefaultLanguages.add(lang);
				}
			}
			Collections.sort(defaultLanguages);
		}
		adapter = new LanguagesArrayAdapter(this, defaultLanguages, 
						selectedDefaultLanguages);
		setListAdapter(adapter);
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			FileIO.writeDefaultLanguages(selectedDefaultLanguages);
		} catch (IOException e) {
			// Not much that can be done if writing fails, except perhaps
			// toasting the user.
		}
		/*
		try {
			FileIO.writeDefaultLanguages(defaultLanguages);
		} catch (IOException e) {
			// Not much that can be done if writing fails, except perhaps
			// toasting the user.
		}
		*/
	}

	@Override
	public void onResume() {
		super.onResume();
		
		/*
		defaultLanguages = FileIO.readDefaultLanguages();
		ArrayAdapter<Language> adapter =
				new SpeakerLanguagesArrayAdapter(this, defaultLanguages, 
						selectedDefaultLanguages);
		setListAdapter(adapter);
		*/
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
					selectedDefaultLanguages.add(language);
				}
				try {
					FileIO.writeDefaultLanguages(defaultLanguages);
				} catch (IOException e) {
					//Not much we can do if it won't write. Maybe toast an
					//error?
				}
			}
		}
	}

	
	static final int SELECT_LANGUAGE = 0;
	// Languages shown in the list, and selected by the user.
	private List<Language> defaultLanguages;
	private List<Language> selectedDefaultLanguages = new ArrayList<Language>();
	ArrayAdapter<Language> adapter;
}
