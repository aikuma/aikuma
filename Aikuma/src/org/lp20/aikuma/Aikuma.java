package org.lp20.aikuma;

import android.content.Context;
import android.provider.Settings.Secure;
import java.util.List;
import java.io.IOException;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.util.FileIO;

/**
 * Sources and caveats:
 * http://stackoverflow.com/questions/987072/using-application-context-everywhere
 * http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 */

public class Aikuma extends android.app.Application {

	private static Aikuma instance;
	private static List<Language> languages;

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

	public static List<Language> getLanguages() {
		if (languages == null) {
			loadLanguages();
			while (languages == null) {
				//Wait patiently.
			}
		}
		return languages;
	}

	public static void loadLanguages() {
		if (languages == null) {
			if (loadLangCodesThread == null || !loadLangCodesThread.isAlive()) {
				loadLangCodesThread = new Thread(new Runnable() {
					public void run() {
						try {
							languages = FileIO.readLangCodes(getContext().getResources());
						} catch (IOException e) {
							throw new RuntimeException("Cannot load languages");
						}
					}
				});
				loadLangCodesThread.start();
			}
		}
	}

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

