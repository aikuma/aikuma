/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.http.Server;
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
			case R.id.start_http_server:
				if (httpStarted) {
					Server.getServer().stop();
					httpStarted = false;
				}
				else {
					String msg;
					Resources res = activity.getResources();
					try {
						Server.setPort(httpPort);
						Server.getServer().start();
						httpStarted = true;
						msg = String.format(res.getString(R.string.http_dialog_success, httpPort));
					}
					catch (java.io.IOException e) {
						msg = res.getString(R.string.http_dialog_failure);
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setTitle(R.string.http_dialog_title).setMessage(msg);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				return true;
			default:
				return true;
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.start_http_server);
		if (httpStarted == true) {
			item.setTitle(R.string.http_menu_stop_server);
		}
		else {
			item.setTitle(R.string.http_menu_start_server);
		}
		return true;
	}
	
	public boolean safeOnOptionsItemSelected(MenuItem item,
			String safeActivityTransitionMessage) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				safeGoToMainActivity(safeActivityTransitionMessage);
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
	public void safeGoToMainActivity(String safeActivityTransitionMessage) {
		String message = DEFAULT_MESSAGE;
		if (safeActivityTransitionMessage != null) {
			message = safeActivityTransitionMessage;
		}
		new AlertDialog.Builder(activity)
				.setMessage(message)
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
	public void safeGoBack(String safeActivityTransitionMessage) {
		String message = DEFAULT_MESSAGE;
		if (safeActivityTransitionMessage != null) {
			message = safeActivityTransitionMessage;
		}
		new AlertDialog.Builder(activity)
				.setMessage(message)
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
	private String DEFAULT_MESSAGE = "This will discard the new data. Are you sure?";
	private boolean httpStarted = false;
	private int httpPort = 8080;
}
