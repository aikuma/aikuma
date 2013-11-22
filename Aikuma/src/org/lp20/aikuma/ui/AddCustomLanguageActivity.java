package org.lp20.aikuma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;

public class AddCustomLanguageActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_custom_language);
	}

	public void addCustomLanguage(View view) {
		EditText nameField = (EditText) findViewById(R.id.languageName);
		String name = nameField.getText().toString();
		
		Intent intent = new Intent();
		intent.putExtra("language", new Language(name, ""));
		setResult(RESULT_OK, intent);
		this.finish();
	}
}
