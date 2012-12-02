package au.edu.unimelb.boldapp;

import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;

import android.util.Log;

/**
 * Class to contain our (minimal number of) global variables.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class GlobalState {
	/**
	 * The user that is currently selected to be the author of new recordings
	 * and respeakings
	 */
	private static User currentUser;

	/**
	 * An array of all the users.
	 */
	private static User[] users;

	/**
	 * A map from UUIDs to users.
	 */
	private static HashMap<UUID, User> userMap;

	/**
	 * An array of all the recordings.
	 */
	private static Recording[] recordings;

	/**
	 * A map from UUIDs to recordings.
	 */
	private static HashMap<UUID, Recording> recordingMap;

	/**
	 * currentUser accessor
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * currentUser mutator
	 */
	public static void setCurrentUser(User currentUser) {
		GlobalState.currentUser = currentUser;
	}

	/**
	 * users accessor
	 */
	public static User[] getUsers() {
		return GlobalState.users;
	}
	
	/**
	 * users mutator; creates a userMap while setting users.
	 */
	public static void setUsers(User[] users) {
		GlobalState.users = users;
		HashMap<UUID, User> userMap = new HashMap();
		for (int i=0; i < users.length; i++) {
			userMap.put(users[i].getUuid(), users[i]);
		}
		GlobalState.userMap = userMap;
	}

	/**
	 * userMap accessor
	 */
	public static HashMap<UUID, User> getUserMap() {
		return GlobalState.userMap;
	}

	/**
	 * recordings mutator
	 */
	public static void setRecordings(Recording[] recordings) {
		GlobalState.recordings = recordings;
		HashMap<UUID, Recording> recordingMap = new HashMap();
		for (int i=0; i < recordings.length; i++) {
			recordingMap.put(recordings[i].getUuid(), recordings[i]);
		}
		GlobalState.recordingMap = recordingMap;
	}
	
	/**
	 * default recordings accessor
	 */
	public static Recording[] getRecordings() {
		return GlobalState.recordings;
	}

	/**
	 * recordings accessor
	 *
	 * @param	sortBy	String with values either "alphabetical" or "date"
	 * indicationg how the caller wants the recordings sorted.
	 */
	public static Recording[] getRecordings(String sortBy) {
		Arrays.sort(GlobalState.recordings, new RecordingComparator(sortBy));
		return GlobalState.recordings;
	}

	/**
	 * recordingMap accessor
	 */
	public static HashMap<UUID, Recording> getRecordingMap() {
		return GlobalState.recordingMap;
	}

	/**
	 * Loads the users from the bold directory.
	 */
	public static boolean loadUser() {
		return false;
	}
}
