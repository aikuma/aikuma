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
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

/**
 * The activity that allows default languages to be specified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class DefaultLanguagesActivity extends AikumaListActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.default_languages);
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			FileIO.writeDefaultLanguages(defaultLanguages);
		} catch (IOException e) {
			// Not much that can be done if writing fails, except perhaps
			// toasting the user.
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		defaultLanguages = FileIO.readDefaultLanguages();
		ArrayAdapter<Language> adapter =
				new SpeakerLanguagesArrayAdapter(this, defaultLanguages);
		setListAdapter(adapter);
	}

	public void onAddISOLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	public void onAddCustomLanguageButton(View view) {
		Intent intent = new Intent(this, AddCustomLanguageActivity.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent
			intent) {
		if (requestCode == SELECT_LANGUAGE) {
			if (resultCode == RESULT_OK) {
				Language language =
						(Language) intent.getParcelableExtra("language");
				if (!defaultLanguages.contains(language)) {
					defaultLanguages.add(language);
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
	private List<Language> defaultLanguages;
}
