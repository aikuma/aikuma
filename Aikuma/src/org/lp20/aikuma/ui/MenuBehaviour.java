/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.util.AikumaSettings;

/**
 * Class that unifies some inter-activity navigation code.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MenuBehaviour {

	/**
	 * The sole constructor, requires an activity.
	 *
	 * @param	activity	The activity whose menu behaviour we are defining.
	 */
	public MenuBehaviour(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Creates the options menu appropriate for the activity.
	 *
	 * @param	menu	The menu in question.
	 * @return	Always true, so that when Activity.onCreateOptionsMenu uses
	 * this implementation, the display is shown.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		MenuInflater inflater = activity.getMenuInflater();
		if (activity instanceof MainActivity) {
			inflater.inflate(R.menu.main, menu);
			//((MainActivity)activity).setUpSearchInterface(menu);
			if(AikumaSettings.getCurrentUserId() != null)
				setSignInState(true);
		} else {
			inflater.inflate(R.menu.other, menu);
		}
		return true;
	}

	/**
	 * Defines what happens when an menu item is selected and safe transitions
	 * are off.
	 *
	 * @param	item	The menu item selected.
	 * @return	Always true.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				goToMainActivity();
				return true;
			case R.id.search:
				if(AikumaSettings.getCurrentUserToken() == null) {
					Aikuma.showAlertDialog(activity,
							"You need to be online using your account");
					return true;
				}
				intent = new Intent(activity, CloudSearchActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.record:
				if(AikumaSettings.getCurrentUserId() == null) {
					Aikuma.showAlertDialog(activity,
							"You need to select your account");
					return true;
				}
				intent = new Intent(activity, RecordActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.speakers:
				if(AikumaSettings.getCurrentUserId() == null) {
					Aikuma.showAlertDialog(activity,
							"You need to select your account");
					return true;
				}
				intent = new Intent(activity, MainSpeakersActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.help:
				intent = new Intent(activity, HowtoActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.language_setting_menu:
				intent = new Intent(activity, DefaultLanguagesActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.settings:
				intent = new Intent(activity, SettingsActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.about:
				intent = new Intent(activity, AboutActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.start_http_server:
				intent = new Intent(activity, HttpServerActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.audio_import_menu:
				((MainActivity)activity).audioImport(null);
				return true;
			case R.id.gplus_signin_menu:
				if(signInState) {
					((MainActivity)activity).clearAccountToken();
				} else {
					((MainActivity)activity).getAccountToken();
				}
				
				return true;
			case R.id.cloud_sync_menu:
				((MainActivity)activity).syncRefresh(true);
				//intent = new Intent(activity, CloudSyncSettingsActivity.class);
				//activity.startActivity(intent);
				return true;
			case R.id.cloud_sync_setting_menu:
				intent = new Intent(activity, CloudSettingsActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.ftp_sync_setting_menu:
				intent = new Intent(activity, SyncSettingsActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.indexing_menu:
				try {
					Recording.indexAll();
				} catch (IOException e) {
					Aikuma.showAlertDialog(activity, e.getMessage());
				}
				return true;
			case R.id.debugInfo:
				intent = new Intent(activity, DebugInfo.class);
				activity.startActivity(intent);
				return true;
			default:
				return true;
		}
	}

	/**
	 * Defines what happens when an menu item is selected and safe transitions
	 * are off.
	 *
	 * @param	item	The menu item selected.
	 * @param	safeActivityTransitionMessage	The message to display warning
	 * about data loss.
	 * @return	Always true.
	 */
	public boolean safeOnOptionsItemSelected(MenuItem item,
			String safeActivityTransitionMessage) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				safeGoToMainActivity(safeActivityTransitionMessage);
				return true;
			case R.id.record:
				if(AikumaSettings.getCurrentUserId() == null) {
					Aikuma.showAlertDialog(activity,
							"You need to select an account to make a recording");
					return true;
				}
				intent = new Intent(activity, RecordActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.help:
				intent = new Intent(activity, HowtoActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.settings:
				intent = new Intent(activity, SettingsActivity.class);
				activity.startActivity(intent);
				return true;
			case R.id.about:
				intent = new Intent(activity, AboutActivity.class);
				activity.startActivity(intent);
				return true;
			default:
				return true;
		}
	}

	/**
	 * Return the MenuItem corresponding to resourceId
	 * @param resourceId	ID of the menu item
	 * @return	the menu-item having resourceId
	 */
	public MenuItem findItem(int resourceId) {
		return menu.findItem(resourceId);
	}
	
	/**
	 * Set if the user signed-in an account
	 * @param state		true(signed-in), false(no sign-in)
	 */
	public void setSignInState(boolean state) {
		this.signInState = state;
		if(state) {
			//TODO: get emailAccount from AikumaSettings
			String signOutString = "Sign-out: "; // + activity.emailAccount;
			findItem(R.id.gplus_signin_menu).setTitle(signOutString);
		} else {
			String signInString = 
					activity.getResources().getString(R.string.gplus_signin_menu_label);
			findItem(R.id.gplus_signin_menu).setTitle(signInString);
		}
	}
	
	/**
	 * Opens the howto from lp20.org in a browser.
	 */
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
	 *
	 * @param	safeActivityTransitionMessage	The string to display in a warning message.
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
	 *
	 * @param	safeActivityTransitionMessage	The string to display in a warning message.
	 * @param	safeBehaviour	Interface having a function required for safe back-button.
	 */
	public void safeGoBack(String safeActivityTransitionMessage, 
			final BackButtonBehaviour safeBehaviour) {
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
								if(safeBehaviour != null)
									safeBehaviour.onSafeBackButton();
								activity.finish();
							}
						})
				.setNegativeButton("Cancel", null)
				.show();
	}

	/**
	 * Interface having a function 
	 * required for safe back-button and defined by other activities
	 * @author Sangyeop Lee
	 *
	 */
	public static interface BackButtonBehaviour {
		/**
		 * The function called for safe back-button action
		 */
		public void onSafeBackButton();
	}
	
	private Activity activity;
	private Menu menu;
	private boolean signInState = false;
	
	private String DEFAULT_MESSAGE = "This will discard the new data. Are you sure?";
}
