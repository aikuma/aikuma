/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import java.util.List;
import java.io.IOException;
import org.lp20.aikuma.model.Language;
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
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    /**
     * Show the warning dialog with the message
     * @param message	the message shown in the dialog
     */
    public static void showAlertDialog(Context activity, String message) {
    	new AlertDialog.Builder(activity).setMessage(message).show();
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
	 * Loads the ISO 639-3 languages.
	 */
	public static void loadLanguages() {
		if (languages == null) {
			if (loadLangCodesThread == null || !loadLangCodesThread.isAlive()) {
				loadLangCodesThread = new Thread(new Runnable() {
					public void run() {
						try {
							languages = FileIO.readLangCodes(getContext().getResources());
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

