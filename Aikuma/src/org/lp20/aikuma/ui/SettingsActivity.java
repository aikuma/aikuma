/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.UsageUtils;

/** The mother activity for settings - hosts buttons that link to various
 * specific settings activities.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SettingsActivity extends AikumaActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		getVersionInfo();
		getUsageInfo();
	}

	private void getVersionInfo() {
		TextView versionField = (TextView) findViewById(R.id.versionField);
		try {
				versionField.setText("Version: " +
						this.getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionName);
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
				//Just leave the textview empty.
		}
	}

	private void getUsageInfo() {
		TextView usageField = (TextView) findViewById(R.id.usageField);
		usageField.setText("used: " + UsageUtils.secondsUsed(16000, 16));
	}

	public void onDefaultLanguagesButton(View view) {
		Intent intent = new Intent(this, DefaultLanguagesActivity.class);
		startActivity(intent);
	}

	public void onSyncSettingsButton(View view) {
		Intent intent = new Intent(this, SyncSettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
}
