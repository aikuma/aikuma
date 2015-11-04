/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

/**
 * Offers a collection of static methods that require a context independently
 * of an Activity.
 *
 * Sources and caveats:
 * http://stackoverflow.com/questions/987072/using-application-context-everywhere
 * http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 * 
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Aikuma extends android.app.Application {

	private static Aikuma instance;
	private static List<Language> languages;
	private static Map<String, String> languageCodeMap;
	private static SharedPreferences preferences;

	/**
	 * The constructor.
	 */
	public Aikuma() {
		instance = this;
	}

	/**
	 * Static method that provides a context when needed by code not bound to
	 * any meaningful context.
	 *
	 * @return	A Context
	 */
	public static Context getContext() {
		return instance;
	}

	/**
	 * Gets the phone model name
	 * 
	 * @return	The device name (manufacturer + model)
	 */
	public static String getDeviceName() {
		return Build.MANUFACTURER + "-" + Build.MODEL;
	}
	
	/**
	 * Gets the android ID of the phone.
	 *
	 * @return	The android ID as a String.
	 */
	public static String getAndroidID() {
		return Secure.getString(
				getContext().getContentResolver(), Secure.ANDROID_ID);
	}

	/**
     * Checks whether the device is currently connected to a network
     * @return	boolean for status
     */
    public static boolean isDeviceOnline() {
    	if(preferences == null) {
    		preferences = 
    				PreferenceManager.getDefaultSharedPreferences(getContext());
    		AikumaSettings.isOnlyWifi = 
    				preferences.getBoolean(AikumaSettings.WIFI_MODE_KEY, true);
    	}
    	
    	ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo;
    	if(AikumaSettings.isNetwork){
    		networkInfo = connMgr.getActiveNetworkInfo();   
    	} else if(AikumaSettings.isOnlyWifi) {
    		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	} else {
    		networkInfo = null;
    	}
    	
    	if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
    	return false;
    }
    
    /**
     * Checks if Wifi is enabled
     * @return	true if Wifi is enabled
     */
    public static boolean isWifiEnabled() {
    	WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
    	return wifiMgr.isWifiEnabled();
    }
    
    /**
     * Check if the file is already archived/in the progress of archiving or not
     * 
     * @param emailAccount	Owner account of the file
     * @param fm			The file's model
     * @return				boolean of the archive status
     */
    public static boolean isArchived(String emailAccount, FileModel fm) {
    	//TODO: needs to be changed to make a query to fusionindex (because, the file could be archived in the other device)
    	
    	String itemCloudId = fm.getCloudIdentifier(0);
    	
    	Set<String> approvedSet;
    	Set<String> archivedSet;
    	int archiveProgress = -1;
    	
    	SharedPreferences privatePreferences = 
    			getContext().getSharedPreferences(emailAccount, MODE_PRIVATE);
    	String fileType = fm.getFileType();
    	if(fileType.equals("source") || fileType.equals("respeaking")) {
    		approvedSet = (HashSet<String>) privatePreferences.getStringSet(
    				AikumaSettings.APPROVED_RECORDING_KEY, new HashSet<String>());
    		archivedSet = (HashSet<String>) privatePreferences.getStringSet(
    				AikumaSettings.ARCHIVED_RECORDING_KEY, new HashSet<String>());
		} else if(fileType.equals("speaker")) {
			approvedSet = (HashSet<String>) privatePreferences.getStringSet(
    				AikumaSettings.APPROVED_SPEAKERS_KEY, new HashSet<String>());
			archivedSet = (HashSet<String>) privatePreferences.getStringSet(
					AikumaSettings.ARCHIVED_SPEAKERS_KEY, new HashSet<String>());
		} else {
			approvedSet = (HashSet<String>) privatePreferences.getStringSet(
    				AikumaSettings.APPROVED_OTHERS_KEY, new HashSet<String>());
			archivedSet = (HashSet<String>) privatePreferences.getStringSet(
					AikumaSettings.ARCHIVED_OTHERS_KEY, new HashSet<String>());
		}
    	
    	if(approvedSet.contains(itemCloudId)) {
    		String[] requestArchiveState = 
					privatePreferences.getString(itemCloudId, "").split("\\|");
			archiveProgress = 
					Integer.parseInt(requestArchiveState[1]);
    	}

    	return archivedSet.contains(itemCloudId) || (archiveProgress >= 0 && archiveProgress <= 3);
    }
    
    /**
	 * Sync the device with Google-Cloud
	 * 
	 * @param	activity	The activity which triggers sync
	 * @param	forceSync	Force the cloud-sync
	 */
	public static void syncRefresh(Activity activity, boolean forceSync) {
		if(forceSync) {
			if(!Aikuma.isDeviceOnline()) {
				Aikuma.showAlertDialog(activity, "Network needs to be connected");
				return;
			} else if(AikumaSettings.getCurrentUserToken() == null) {
				Aikuma.showAlertDialog(activity, 
						"You need to connect to Google-Drive with your account");
				return;
			} else {
				Intent syncIntent = new Intent(activity, GoogleCloudService.class);
				syncIntent.putExtra(GoogleCloudService.ACTION_KEY, "sync");
				syncIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
						AikumaSettings.getCurrentUserId());
				syncIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
						AikumaSettings.getCurrentUserToken());
				syncIntent.putExtra("forceSync", forceSync);
				activity.startService(syncIntent);
			}
		} else {
			SharedPreferences settings = activity.getSharedPreferences(
					AikumaSettings.getCurrentUserId(), MODE_PRIVATE);
	
			int sz = 0;
			sz += settings.getStringSet(AikumaSettings.APPROVED_RECORDING_KEY, 
					new HashSet<String>()).size();
			sz += settings.getStringSet(AikumaSettings.APPROVED_SPEAKERS_KEY,
					new HashSet<String>()).size();
			sz += settings.getStringSet(AikumaSettings.APPROVED_OTHERS_KEY,
					new HashSet<String>()).size();
			sz += settings.getStringSet(AikumaSettings.DOWNLOAD_RECORDING_KEY, 
					new HashSet<String>()).size();
			sz += settings.getStringSet(AikumaSettings.DOWNLOAD_SPEAKERS_KEY,
					new HashSet<String>()).size();
			sz += settings.getStringSet(AikumaSettings.DOWNLOAD_OTHERS_KEY,
					new HashSet<String>()).size();

			if(sz > 0) {
	    		// If there are items to be uploaded,
				// start the GoogleCloud upload service 
	    		Intent intent = new Intent(activity, 
	    				GoogleCloudService.class);
	    		intent.putExtra(GoogleCloudService.ACTION_KEY, "retry");
	    		intent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
						AikumaSettings.getCurrentUserId());
				intent.putExtra(GoogleCloudService.TOKEN_KEY, 
						AikumaSettings.getCurrentUserToken());
				activity.startService(intent);
			}
		}
	}
    
    
    /**
     * Return an arraylist of available google-accounts in a device
     * @return	ArrayList of google-accounts
     */
    public static ArrayList<String> getGoogleAccounts() {
    	ArrayList<String> accountList = new ArrayList<String>();
    	
    	Account[] accounts = 
    			AccountManager.get(getContext()).getAccountsByType("com.google");
    	for(Account ac : accounts) {
    		accountList.add(ac.name);
    	}
    	
    	return accountList;
    }
    
    
    /**
     * Show the warning dialog with the message
     * @param activity	The activity where the message will be shown
     * @param message	the message shown in the dialog
     */
    public static void showAlertDialog(Context activity, String message) {
    	new AlertDialog.Builder(activity).setMessage(message).show();
    }
    
    /**
     * Show the confirmation dialog with the message
     * @param activity	The activity where the message will be shown
     * @param message	the message shown in the dialog
     * @param okListener	The callback interface when ok-button is pressed
     */
    public static void showConfirmationDialog(Context activity, String message, 
    		DialogInterface.OnClickListener okListener) {
    	new AlertDialog.Builder(activity)
    		.setMessage(message)
    		.setPositiveButton("OK", okListener)
    		.setNegativeButton("Cancel", null)
    		.show();
    }
	
	/**
	 * Returns the ISO 639-3 languages once they are loaded.
	 * 
	 * @return	the languages
	 */
	public static List<Language> getLanguages() {
		if (languages == null) {
			loadLanguages();
			while (languages == null) {
				//Wait patiently.
			}
		}
		return languages;
	}
	
	/**
	 * Returns the ISO 639-3 language map (code - name)
	 * 
	 * @return	the languageCodeMap
	 */
	public static Map<String, String> getLanguageCodeMap() {
		if (languageCodeMap == null) {
			loadLanguages();
			while (languageCodeMap == null) {
				//Wait patiently
			}
		}
		return languageCodeMap;
	}
	
	

	/**
	 * Loads the ISO 639-3 languages.
	 */
	public static void loadLanguages() {
		if (languages == null) {
			languages = new ArrayList<Language>();
			languageCodeMap = new HashMap<String, String>();
			
			if (loadLangCodesThread == null || !loadLangCodesThread.isAlive()) {
				loadLangCodesThread = new Thread(new Runnable() {
					public void run() {
						try {
							FileIO.readLangCodes(getContext().getResources(), 
									languages, languageCodeMap);
						} catch (IOException e) {
							// This should never happen.
							throw new RuntimeException("Cannot load languages");
						}
					}
				});
				loadLangCodesThread.start();
			}
		}
	}

	/**
	 * The thread used to load the language codes without interrupting the
	 * main thread.
	 */
	public static Thread loadLangCodesThread;
}

/*
public class Aikuma extends android.app.Application {
	
	private static Context context;

	public void onCreate() {
		super.onCreate();
		Aikuma.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return Aikuma.context;
	}

	public static String getAndroidID() {
		return Secure.getString(
				getAppContext().getContentResolver(), Secure.ANDROID_ID);
	}
}
*/

