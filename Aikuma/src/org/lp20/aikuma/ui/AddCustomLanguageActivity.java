/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma2.R;

/**
 * Allows the user to add a custom language as a default language that isn't in
 * ISO 639-3.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class AddCustomLanguageActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_custom_language);
		nameField = (EditText) findViewById(R.id.languageName);
		nameField.addTextChangedListener(emptyTextWatcher);
		addLanguageButton = (Button)
				findViewById(R.id.addCustomLanguageButton);
		addLanguageButton.setEnabled(false);
	}

	/**
	 * When the add custom language button is pressed.
	 *
	 * @param	view	The add custom language button.
	 */
	public void addCustomLanguage(View view) {
		String name = nameField.getText().toString();
		Intent intent = new Intent();
		intent.putExtra("language", new Language(name, ""));
		setResult(RESULT_OK, intent);
		this.finish();
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	// Ensures the language name field has characters in it, and
	// disables/enables the add language button on the fly.
	private TextWatcher emptyTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s,
				int start, int count, int after) {
		}
		public void onTextChanged(CharSequence s,
				int start, int before, int count) {
			if (s.length() == 0) {
				addLanguageButton.setEnabled(false);
			} else {
				addLanguageButton.setEnabled(true);
			}
		}
	};

	private EditText nameField;
	private Button addLanguageButton;
}
