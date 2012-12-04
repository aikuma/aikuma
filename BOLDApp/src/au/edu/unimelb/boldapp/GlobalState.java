package au.edu.unimelb.boldapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	 * A list of all the users.
	 */
	private static List<User> users;

	/**
	 * A map from UUIDs to users.
	 */
	private static HashMap<UUID, User> userMap;

	/**
	 * A list of all the recordings.
	 */
	private static List<Recording> recordings;

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
	public static List<User> getUsers() {
		return GlobalState.users;
	}
	
	/**
	 * users mutator; creates a userMap while setting users.
	 */
	public static void setUsers(List<User> users) {
		GlobalState.users = users;
		HashMap<UUID, User> userMap = new HashMap();
		for (User user : users) {
			userMap.put(user.getUUID(), user);
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
	public static void setRecordings(List<Recording> recordings) {
		GlobalState.recordings = recordings;
		HashMap<UUID, Recording> recordingMap = new HashMap();
		for (Recording recording : recordings) {
			recordingMap.put(recording.getUUID(), recording);
		}
		GlobalState.recordingMap = recordingMap;
	}

	/**
	 * default recordings accessor
	 */
	public static List<Recording> getRecordings() {
		return GlobalState.recordings;
	}

	/**
	 * recordings accessor
	 *
	 * @param	sortBy	String with values either "alphabetical" or "date"
	 * indicationg how the caller wants the recordings sorted.
	 */
	public static List<Recording> getRecordings(String sortBy) {
		java.util.Collections.sort(GlobalState.recordings, new RecordingComparator(sortBy));
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
	public static boolean loadUsers() {
		try {
			setUsers(FileIO.readUsers());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Loads the users from the bold directory.
	 */
	public static boolean loadRecordings() {
		try {
			setRecordings(FileIO.readRecordings());
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
