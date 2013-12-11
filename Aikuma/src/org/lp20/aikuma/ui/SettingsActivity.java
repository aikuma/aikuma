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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.ServerCredentials;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.SyncUtil;

/**
 * The activity that allows default languages and network settings to be
 * modified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SettingsActivity extends AikumaListActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		TextView versionField = (TextView) findViewById(R.id.versionField);
		try {
			versionField.setText(this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			//Just leave the textview empty.
		}
		ipAddressField = (EditText) findViewById(R.id.ipAddress);
		usernameField = (EditText) findViewById(R.id.username);
		passwordField = (EditText) findViewById(R.id.password);
		toggleButton = (ToggleButton) findViewById(R.id.syncToggle);
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			FileIO.writeDefaultLanguages(defaultLanguages);
			commitServerCredentials();
		} catch (IOException e) {
			// Not much that can be done if writing fails, except perhaps
			// toasting the user.
		}
	}

	private void commitServerCredentials() throws IOException {
		try {
			ServerCredentials serverCredentials =
					new ServerCredentials(
							ipAddressField.getText().toString(),
							usernameField.getText().toString(),
							passwordField.getText().toString(),
							syncActivated);
			serverCredentials.write();
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		defaultLanguages = FileIO.readDefaultLanguages();
		ArrayAdapter<Language> adapter =
				new SpeakerLanguagesArrayAdapter(this, defaultLanguages);
		setListAdapter(adapter);

		try {
			ServerCredentials serverCredentials = ServerCredentials.read();
			ipAddressField.setText(serverCredentials.getIPAddress());
			usernameField.setText(serverCredentials.getUsername());
			passwordField.setText(serverCredentials.getPassword());
			toggleButton.setChecked(serverCredentials.getSyncActivated());
			syncActivated = serverCredentials.getSyncActivated();
		} catch (IOException e) {
			//If reading fails, then no text is put into the fields.
		}
	}

	public void onAddISOLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	public void onSyncNowButton(View view) {
		try {
			commitServerCredentials();
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		SyncUtil.syncNow();
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

	public void onToggleClicked(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		if (on) {
			syncActivated = true;
		} else {
			syncActivated = false;
		}
		try {
			commitServerCredentials();
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Constant to represent the request code for the LanguageFilterList calls.
	 */
	static final int SELECT_LANGUAGE = 0;
	private List<Language> defaultLanguages;
	static Map langCodeMap;
	static Thread loadLangCodesThread;
	private EditText ipAddressField;
	private EditText usernameField;
	private EditText passwordField;
	private boolean syncActivated;
	private ToggleButton toggleButton;
}
