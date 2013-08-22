package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

/**
 * The activity that allows default languages and network settings to be
 * modified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SettingsActivity extends ListActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.mainlist:
				intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				SettingsActivity.this.finish();
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onAddLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
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

	/**
	 * Constant to represent the request code for the LanguageFilterList calls.
	 */
	static final int SELECT_LANGUAGE = 0;
	private List<Language> defaultLanguages;
	static Map langCodeMap;
	static Thread loadLangCodesThread;
}
