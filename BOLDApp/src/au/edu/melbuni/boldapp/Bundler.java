package au.edu.melbuni.boldapp;

import android.app.Activity;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;


/*
 * This class helps with saving state.
 */
public class Bundler {
	
	public static void load(Activity activity) {
		((BoldApplication) activity.getApplication()).load();
	}
	
	public static void save(Activity activity) {
		((BoldApplication) activity.getApplication()).save();
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

	public static Users getUsers(Activity activity) {
		BoldApplication application = getApplication(activity);
		return application.getUsers();
	}
	
	public static Timeline getCurrentTimeline(Activity activity) {
		return ((BoldApplication) activity.getApplication()).getCurrentTimeline();
	}

	public static void setCurrentTimeline(Activity activity, Timeline timeline) {
		((BoldApplication) activity.getApplication()).setCurrentTimeline(timeline);
	}
	
	public static void addTimeline(Activity activity, Timeline timeline) {
		BoldApplication application = getApplication(activity);
		application.addTimeline(timeline);
	}
	
	public static Timelines getTimelines(Activity activity) {
		BoldApplication application = getApplication(activity);
		return application.getTimelines();
	}

	// Returns the BoldApplication from the given activity.
	//
	protected static BoldApplication getApplication(Activity activity) {
		return (BoldApplication) activity.getApplication();
	}

}
