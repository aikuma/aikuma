package au.edu.unimelb.aikuma.util;

import android.content.Context;

/**
 * Source and caveats:
 * http://stackoverflow.com/questions/987072/using-application-context-everywhere
 */
public class ContextSupplier extends android.app.Application {

	private static ContextSupplier instance;

	public ContextSupplier() {
		instance = this;
	}

	public static Context getContext() {
		return instance;
	}
}
