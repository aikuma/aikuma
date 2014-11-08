/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
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
import android.widget.CheckBox;
import java.io.IOException;
import org.lp20.aikuma.model.ServerCredentials;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.SyncUtil;

/**
 * The activity that allows network settings to be modified.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SyncSettingsActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_settings);
		ipAddressField = (EditText) findViewById(R.id.ipAddress);
		usernameField = (EditText) findViewById(R.id.username);
		passwordField = (EditText) findViewById(R.id.password);
		automaticSyncCheckBox = (CheckBox) findViewById(R.id.automaticSync);
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			commitServerCredentials();
		} catch (IOException e) {
			// Not much that can be done if writing fails, except perhaps
			// toasting the user.
		}
	}

	// Writes the server credentials to file for later use.
	private void commitServerCredentials() throws IOException {
		try {
			ServerCredentials serverCredentials =
					new ServerCredentials(
							ipAddressField.getText().toString(),
							usernameField.getText().toString(),
							passwordField.getText().toString(),
							syncActivated, lastSyncSuccessDate);
			serverCredentials.write();
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		SyncUtil.setSyncSettingsActivity(this);

		try {
			ServerCredentials serverCredentials = ServerCredentials.read();
			if (serverCredentials.getIPAddress().equals("")) {
				ipAddressField.setText("192.168.0.1");
			} else {
				ipAddressField.setText(serverCredentials.getIPAddress());
			}
			if (serverCredentials.getUsername().equals("")) {
				usernameField.setText("admin");
			} else {
				usernameField.setText(serverCredentials.getUsername());
			}
			if (serverCredentials.getPassword().equals("")) {
				passwordField.setText("admin");
			} else {
				passwordField.setText(serverCredentials.getPassword());
			}
			automaticSyncCheckBox.setChecked(
					serverCredentials.getSyncActivated());
			syncActivated = serverCredentials.getSyncActivated();
			lastSyncSuccessDate = serverCredentials.getLastSyncDate();
		} catch (IOException e) {
			//If reading fails, then no text is put into the fields, except for
			//the default IP.
			ipAddressField.setText("192.168.0.1");
			usernameField.setText("admin");
			passwordField.setText("admin");
		}
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	/**
	 * Forces a sync to happen immediately.
	 *
	 * @param	view	The sync now button.
	 */
	public void onSyncNowButton(View view) {
		try {
			commitServerCredentials();
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		SyncUtil.syncNow();
	}

	/**
	 * Defines the behaviour that should occur when the sync checkbox is
	 * toggled.
	 *
	 * @param	view	The sync CheckBox.
	 */
	public void onCheckBoxClicked(View view) {
		boolean on = ((CheckBox) view).isChecked();
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
	
	private EditText ipAddressField;
	private EditText usernameField;
	private EditText passwordField;
	private boolean syncActivated;
	private String lastSyncSuccessDate;
	private CheckBox automaticSyncCheckBox;
}
