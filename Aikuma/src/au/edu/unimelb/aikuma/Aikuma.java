package au.edu.unimelb.aikuma;

import android.content.Context;
import android.provider.Settings.Secure;

/**
 * Sources and caveats:
 * http://stackoverflow.com/questions/987072/using-application-context-everywhere
 * http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 */

public class Aikuma extends android.app.Application {

	private static Aikuma instance;

	public Aikuma() {
		instance = this;
	}

	public static Context getContext() {
		return instance;
	}

	public static String getAndroidID() {
		return Secure.getString(
				getContext().getContentResolver(), Secure.ANDROID_ID);
	}

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
