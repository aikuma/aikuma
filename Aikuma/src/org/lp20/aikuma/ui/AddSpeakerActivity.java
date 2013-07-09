package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

public class AddSpeakerActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker);
		try {
			languages = FileIO.readDefaultLanguages();
		} catch (IOException e) {
			// If no default languages can be loaded, then there's not much
			// that can be done. Maybe pop some toast up for the user?
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i("aa", "OKAY5");
		ArrayAdapter<Language> adapter = 
				new SpeakerLanguagesArrayAdapter(this, languages);
		setListAdapter(adapter);
		Log.i("aa", "OKAY6");
	}

	public void onAddLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent
			intent) {
		if (requestCode == SELECT_LANGUAGE) {
			if (resultCode == RESULT_OK) {
				languages.add((Language)
						intent.getParcelableExtra("language"));
				Log.i("langs", "langs");
			}
		}
	}

	static final int SELECT_LANGUAGE = 0;
	private List<Language> languages = new ArrayList<Language>();
}
