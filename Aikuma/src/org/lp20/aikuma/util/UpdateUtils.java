/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Recording;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Class dealing with updates according to version number
 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 *
 */
public class UpdateUtils {
	
	private static final String TAG = "UpdateUtils";
	
	private Context context;
	
	// Default Google account input by owner
	private String defaultAccount;
	// Current version-name of the application(v0x)
	private String currentVersionName;
	private String nextVersionName;
	
	/**
	 * Constructor for updateUtil
	 * @param context	Activity class where update will be performed
	 */
	public UpdateUtils(Context context) {
		this.context = context;
	}
	
	/**
	 * Update File structure by using default owner_id input by owner
	 */
	public void update() {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(context);
		currentVersionName = 
				settings.getString(AikumaSettings.SETTING_VERSION_KEY, "v00");
		Integer currentVersionNum = 
				Integer.parseInt(currentVersionName.substring(1));
		nextVersionName = AikumaSettings.getLatestVersion(); 
		
		switch(currentVersionNum) {
		case 0:
			AccountManager manager = AccountManager.get(context); 
	        Account[] accounts = manager.getAccountsByType("com.google"); 
	        if(accounts.length > 0) {
	        	//TODO: How to decide default-account
	        	//defaultAccount = 
	    		//		settings.getString(AikumaSettings.SETTING_OWNER_ID_KEY, "");
	        	//defaultAccount = accounts[0].name;
	        	
	        	updateVersion(0);
	        } else {
	        	//TODO: How to input default-account
	        	//showAccountInputDialog();
	        }
			return;
		default:
			return;		
		}
	}
	
	private void updateVersion(final Integer currentVersionNum) {
		Log.i(TAG, currentVersionNum + ": " + defaultAccount);
		switch(currentVersionNum) {
		case 0:
			String progressText = String.format("Updating to Aikuma %s...", nextVersionName);
			((MainActivity)context).showProgressDialog(progressText);
			updateFileStructure();
        	updateRecordingsMetadata(currentVersionNum);
        	
        	//saveInSettings(AikumaSettings.SETTING_OWNER_ID_KEY, defaultAccount);
        	saveInSettings(AikumaSettings.SETTING_VERSION_KEY, nextVersionName);
        	((MainActivity)context).dismissProgressDialog();
        	
        	AikumaSettings.setUserId(defaultAccount);
			return;
		}
	}
	
	/**
	 * Update the metadata of all recordings
	 * @param currentVersionNum	Current version number of the installed application
	 */
	private void updateRecordingsMetadata(Integer currentVersionNum) {
		Map<String, Object> newJSONFields = new HashMap<String, Object>();
		Map<String, String> newJSONKeys = new HashMap<String, String>();
		newJSONFields.put(AikumaSettings.SETTING_OWNER_ID_KEY, defaultAccount);
		newJSONFields.put(AikumaSettings.SETTING_VERSION_KEY, nextVersionName);
		newJSONKeys.put("recording", "item_id");
		newJSONKeys.put("groupId", "item_id");
		Recording.updateAll(currentVersionNum, newJSONFields, newJSONKeys);
	}

	/**
	 * Change the file structure
	 */
	private void updateFileStructure() {
		File srcDir = FileIO.getAppRootPath();
		File destDir = FileIO.getOwnerPath(nextVersionName, defaultAccount);
		Log.i(TAG, destDir.getAbsolutePath());
		
		File recordingsDir = new File(srcDir, "recordings");
		File socialDir = new File(srcDir, "social");
		File viewDir = new File(srcDir, "views");
		
		try {
			if(recordingsDir.exists()) {
				FileUtils.moveDirectoryToDirectory(recordingsDir, destDir, true);
			}
			if(socialDir.exists()) {
				FileUtils.moveDirectoryToDirectory(socialDir, destDir, true);
			}
			if(viewDir.exists()) {
				FileUtils.moveDirectoryToDirectory(viewDir, destDir, true);
			}
			File newRecordingsDir = new File(destDir, "recordings");
			newRecordingsDir.renameTo(new File(destDir, "items"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "File-moving failed:" + e.toString());
		}
	}
	
	
	/**
	 * Save the account/version_num in "settings" SharedPreference
	 * 
	 * @param account	google account(email address)
	 */
	private void saveInSettings(String key, String value) {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * Show the dialgoue to get owner's default google account
	 */
	public void showAccountInputDialog() {
		final EditText accountInput = new EditText(context);
		final AlertDialog dialog = new AlertDialog.Builder(context)
    	.setTitle("Enter your Google account")
    	.setView(accountInput)
    	.setPositiveButton("OK", 
    			new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			defaultAccount = accountInput.getText().toString();

    			updateVersion(0);
    		}
    	})
    	.create();
		
		// E-mail input validation (if not, ok button is diabled)
    	accountInput.setInputType(InputType.TYPE_CLASS_TEXT | 
    			InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    	accountInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String account = s.toString();
				if(android.util.
						Patterns.EMAIL_ADDRESS.matcher(account).matches()) {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}
    	});
    	
    	dialog.show();
    	dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}

}
