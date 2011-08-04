package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Environment;

/*
 * This class helps with saving state.
 */
public class Bundler {
	
	public static String getBasePath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/bold/";
	}
	
	public static void saveNewUser(Activity activity, User user) {
		setCurrentUser(activity, user);
		addUser(activity, user);
	}
	
	public static User getCurrentUser(Activity activity) {
		return ((BoldApplication) activity.getApplication()).getCurrentUser();
	}
	public static void setCurrentUser(Activity activity, User user) {
		((BoldApplication) activity.getApplication()).setCurrentUser(user);
	}
	
	public static void addUser(Activity activity, User user) {
		BoldApplication application = getApplication(activity);
		application.addUser(user);
	}
	
	public static ArrayList<User> getUsers(Activity activity) {
		BoldApplication application = getApplication(activity);
		return application.getUsers();
	}
	
	// Returns the BoldApplication from the given activity.
	//
	protected static BoldApplication getApplication(Activity activity) {
		return (BoldApplication) activity.getApplication();
	}
	
}
