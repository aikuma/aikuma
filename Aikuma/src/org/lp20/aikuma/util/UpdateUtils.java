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
	private String versionName;
	
	public UpdateUtils(Context context) {
		this.context = context;
	}
	
	/**
	 * Update File structure by using default owner_id input by owner
	 */
	public void update() {
		SharedPreferences settings = 
				context.getSharedPreferences(AikumaSettings.SETTING_NAME, 0);
		versionName = settings.getString("version", "v00");
		Integer versionNum = Integer.parseInt(versionName.substring(1));
		
		switch(versionNum) {
		case 0:
			AccountManager manager = AccountManager.get(context); 
	        Account[] accounts = manager.getAccountsByType("com.google"); 
	        if(accounts.length > 0) {
	        	defaultAccount = accounts[0].name;
	        	
	        	updateVersion(0);
	        } else {
	        	showAccountInputDialog();
	        }
			return;
		default:
			return;		
		}
	}
	
	private void updateVersion(final Integer versionNum) {
		switch(versionNum) {
		case 0:
			new Thread() {
				public void run() {
					((MainActivity)context).showProgressDialog("Updating to Aikuma v01...");
					updateFileStructure();
		        	updateRecordingsMetadata(versionNum);
		        	
		        	saveInSettings("ownerID", defaultAccount);
		        	saveInSettings("version", "v01");
		        	((MainActivity)context).runOnUiThread(new Runnable() {
		        		public void run() {
		        			((MainActivity)context).dismissProgressDialog();
		        		}
		        	});
		        	
				}
			}.start();
        	
        	AikumaSettings.setOwnerId(defaultAccount);
			return;
		}
	}
	
	/**
	 * Update the metadata of all recordings
	 * @param versionNum	Current version number of the installed aplication
	 */
	private void updateRecordingsMetadata(Integer versionNum) {
		Map<String, Object> newJSONFields = 
				new HashMap<String, Object>();
		newJSONFields.put("ownerID", defaultAccount);
		newJSONFields.put("version", versionName);
		Recording.updateAll(versionNum, newJSONFields);
	}
	
	/**
	 * Change the file structure
	 */
	private void updateFileStructure() {
		File srcDir = FileIO.getAppRootPath();
		File destDir = FileIO.getOwnerPath(versionName, defaultAccount);
		Log.i(TAG, destDir.getAbsolutePath());
		
		File recordingsDir = new File(srcDir, "recordings");
		File socialDir = new File(srcDir, "social");
		File viewDir = new File(srcDir, "views");
		
		try {
			FileUtils.moveDirectoryToDirectory(recordingsDir, destDir, true);
			FileUtils.moveDirectoryToDirectory(socialDir, destDir, true);
			FileUtils.moveDirectoryToDirectory(viewDir, destDir, true);
			
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
				context.getSharedPreferences(AikumaSettings.SETTING_NAME, 0);
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
