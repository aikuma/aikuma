package au.edu.unimelb.aikuma;

import android.content.res.Resources;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	public static void loadUsers() {
		setUsers(FileIO.readUsers());
	}

	/**
	 * Loads the users from the bold directory.
	 */
	public static void loadRecordings() {
		setRecordings(FileIO.readRecordings());
	}

	/**
	 * The mapping from language names to codes.
	 */
	private static Map langCodeMap;

	/**
	 * langCodeMap accesor. Ensures that langCodeMap is not null.
	 */
	public static Map getLangCodeMap(Resources resources) {
		if (langCodeMap == null) {
			if (GlobalState.loadLangCodesThread == null ||
					!GlobalState.loadLangCodesThread.isAlive()) {
				loadLangCodeMap(resources);
			}
			while (langCodeMap == null) {
			}
		}
		return langCodeMap;
	}

	/**
	 * langCodeMap mutator
	 */
	public static void setLangCodeMap(Map langCodeMap) {
		GlobalState.langCodeMap = langCodeMap;
	}

	private static Thread loadLangCodesThread;

	/**
	 * loads the language code map
	 *
	 * @param	resources	resources so that the langCodes can be retrieved
	 * from the text file if necessary.
	 */
	public static void loadLangCodeMap(final Resources resources) {
		GlobalState.loadLangCodesThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File mapFile = 
							new File(FileIO.getAppRootPath(), "lang_codes");
					if (mapFile.exists()) {
						FileInputStream fis = new FileInputStream(mapFile);
						ObjectInputStream ois = new ObjectInputStream(fis);
						GlobalState.setLangCodeMap((Map) ois.readObject());
					} else {
						Map langCodeMap =
								FileIO.readLangCodes(resources);
						GlobalState.setLangCodeMap(langCodeMap);
						FileOutputStream fos = new FileOutputStream(mapFile);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(langCodeMap);
					}
				} catch (IOException e) {
					//This is bad.
				} catch (ClassNotFoundException e) {
					//This is bad.
				}
			}
		});
		GlobalState.loadLangCodesThread.start();
	}
}
