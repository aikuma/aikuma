/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.R;

/**
 * Class that unifies some inter-activity navigation code.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MenuBehaviour {

	public MenuBehaviour(Activity activity) {
		this.activity = activity;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = activity.getMenuInflater();
		if (activity instanceof MainActivity) {
			inflater.inflate(R.menu.main, menu);
		} else {
			inflater.inflate(R.menu.other, menu);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("issue103", "hello, " + item.getItemId());
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				goToMainActivity();
				return true;
			case R.id.record:
				intent = new Intent(activity, RecordActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.speakers:
				intent = new Intent(activity, MainSpeakersActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.help:
				openHelpInBrowser();
				return true;
			case R.id.settings:
				intent = new Intent(activity, SettingsActivity.class);
				activity.startActivity(intent);
				//openSettingsActivity;
				return true;
			default:
				return true;
		}
	}

	public boolean safeOnOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				safeGoToMainActivity();
				return true;
			case R.id.record:
				intent = new Intent(activity, RecordActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.help:
				openHelpInBrowser();
				return true;
			case R.id.settings:
				intent = new Intent(activity, SettingsActivity.class);
				activity.startActivity(intent);
				//openSettingsActivity;
				return true;
			default:
				return true;
		}
	}

	private void openHelpInBrowser() {
		Uri uri = Uri.parse("http://lp20.org/aikuma/howto.html");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		activity.startActivity(intent);
	}

	/**
	 * Simply transitions to the MainActivity popping every Activity off the stack.
	 */
	private void goToMainActivity() {
		Intent intent = new Intent(activity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	/**
	 * Transitions to the MainActivity prompting the user with some dialogue
	 * with the supplied text for the message and the positive and negative
	 * buttons.
	 */
	public void safeGoToMainActivity() {
		new AlertDialog.Builder(activity)
				.setMessage("This will discard the new data. Are you sure?")
				.setPositiveButton("Discard",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent =
									new Intent(activity, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								activity.startActivity(intent);
							}
						})
				.setNegativeButton("Cancel", null)
				.show();
	}

	/**
	 * Allows the activity to use the back button to return to the previous
	 * activity in the stack, while ensuring the user is aware they'll lose
	 * data.
	 */
	public void safeGoBack() {
		new AlertDialog.Builder(activity)
				.setMessage("This will discard the new data. Are you sure?")
				.setPositiveButton("Discard",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								activity.finish();
							}
						})
				.setNegativeButton("Cancel", null)
				.show();
	}

	private Activity activity;
}
