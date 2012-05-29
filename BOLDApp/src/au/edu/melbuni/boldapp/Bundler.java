package au.edu.melbuni.boldapp;

import java.io.File;

import android.app.Activity;
import au.edu.melbuni.boldapp.models.RespeakOriginal;
import au.edu.melbuni.boldapp.models.RespeakOriginals;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * This class helps with saving state.
 */
public class Bundler {

	static Player player = null;
	static Recorder recorder = null;

	public static Player getPlayer() {
		if (player == null) {
			player = new Player();
		}
		return player;
	}

	public static Recorder getRecorder() {
		if (recorder == null) {
			recorder = new Recorder();
		}
		return recorder;
	}

	public static void load(Activity activity) {
		getApplication(activity).load();
	}

	public static void save(Activity activity) {
		getApplication(activity).save();
	}

	public static void storeNewUser(Activity activity, User user) {
		setCurrentUser(activity, user);
		addUser(activity, user);
	}

	public static User getCurrentUser(Activity activity) {
		return getApplication(activity).getCurrentUser();
	}

	public static void setCurrentUser(Activity activity, User user) {
		getApplication(activity).setCurrentUser(user);
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
		return getApplication(activity).getCurrentTimeline();
	}

	public static void setCurrentTimeline(Activity activity, Timeline timeline) {
		getApplication(activity).setCurrentTimeline(timeline);
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

	// TODO Move to correct structure!
	//
	public static RespeakOriginals getRespeakOriginals(Activity activity) {
		RespeakOriginals originals = new RespeakOriginals();
		
		String directoryPath = Persister.getBasePath() + "respeak_originals/"; 
		File directory = new File(directoryPath);
		
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				originals.add(new RespeakOriginal(file.getName()));
			}
		}

		return originals;
	}

}
