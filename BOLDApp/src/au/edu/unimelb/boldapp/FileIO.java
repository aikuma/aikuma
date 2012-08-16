package au.edu.unimelb.boldapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import android.os.Environment;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Abstract class that offers various File IO related methods.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class FileIO {
	/**
	 * The application's top level path in the external storage.
	 */
	static final String appRootPath = "bold/";

	/**
	 * The users directory path
	 */
	static final String usersPath = "users/";

	/**
	 * The recordings directory path
	 */
	static final String recordingsPath = "recordings/";

	/**
	 * Returns the absolute path to the application's data.
	 *
	 * @return A string representation of the absolute path to the
	 * application's data
	 */
	public static String getAppRootPath(){
		File external = Environment.getExternalStorageDirectory();
		String path = external.getAbsolutePath() + "/" + appRootPath;
		return path;
	}

	/**
	 * Returns the relative path to the application's users directory
	 *
	 * @return A string representation of the relative path to the
	 * application's users directory
	 */
	public static String getUsersPath(){
		return usersPath;
	}

	/**
	 * Returns the relative path to the application's recordings directory
	 *
	 * @return A string representation of the relative path to the
	 * application's recordings directory
	 */
	public static String getRecordingsPath(){
		return recordingsPath;
	}

	/**
	 * Takes a file path and some data and writes the data into the file in the
	 * bold directory in external storage.
	 *
	 * @param filePath The filename, including parent paths within the bold
	 * dir.
	 * @param data The data that is to be written to the file.
	 */
	public static void write(String filePath, String data) {
		try {
			String absPath = getAppRootPath() + filePath;
			File file = new File(absPath);
			file.getParentFile().mkdirs();
			BufferedWriter bos = new BufferedWriter(new FileWriter(absPath));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Takes a file path relative to the bold directory in external storage and
	 * returns a string containing the file's contents.
	 *
	 * @param	filePath	The filename, including parent paths within the
	 * bold dir.
	 * @return	A string containing the file's contents.
	 */
	public static String read(String filePath) {
		String path = getAppRootPath() + filePath;
		StringBuilder text = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		try {
			Scanner scanner = new Scanner(new FileInputStream(path));
			while (scanner.hasNextLine()){
				text.append(scanner.nextLine() + newLine);
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text.toString();
	}

	/**
	 * Method to load all the users from the users directory into an array of
	 * User objects in GlobalState.
	 */
	public static void loadUsers() {
		// Get an array of all the UUIDs from the "users" directory
		File dir = new File(getAppRootPath() + getUsersPath());
		String[] userUuids = dir.list();

		// Get the user data from the metadata.json files
		User[] users = new User[userUuids.length];
		JSONParser parser = new JSONParser();
		for (int i=0; i < userUuids.length; i++) {
			String jsonStr = read(getUsersPath()
					+ userUuids[i] + "/metadata.json");
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				users[i] = new User(
						UUID.fromString(jsonObj.get("uuid").toString()),
						jsonObj.get("name").toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		GlobalState.setUsers(users);
	}


	/**
	 * Method to load all the recordings from the recordings directory into an
	 * array of Recording objects.
	 *
	 * @return An array of all the recordings as Recording objects.
	 */
	 public static void loadRecordings() {
	 	//Get an array of all the UUIDs from the "recordings" directory
		File dir = new File(getAppRootPath() + getRecordingsPath());
		JSONFilenameFilter fnf = new JSONFilenameFilter();
		String[] recordingUuids = dir.list(fnf);
		/*
		for (int i = 0; i < recordingUuids.length; i++) {
			recordingUuids[i] = recordingUuids[i].replace(".json", "");
			Log.i("yoyoyo", recordingUuids[i]);
		}
		*/

		Recording[] recordings = new Recording[recordingUuids.length];
		JSONParser parser = new JSONParser();
		for (int i=0; i < recordingUuids.length; i++) {
			String jsonStr = read(getRecordingsPath() + recordingUuids[i]);
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				recordings[i] = new Recording(
						UUID.fromString(jsonObj.get("uuid").toString()),
						GlobalState.getUserMap().get(UUID.fromString(
								jsonObj.get("creatorUUID").toString())),
						jsonObj.get("recording_name").toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		GlobalState.setRecordings(recordings);
	 }

	/**
	 * Delete the filename supplied as an argument
	 *
	 * @param	fileName	Name of the file to be deleted.
	 */
	public static void delete(String fileName) {
		File file = new File(getAppRootPath() + fileName);
	}
}
