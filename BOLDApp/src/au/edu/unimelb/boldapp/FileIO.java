package au.edu.unimelb.boldapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.Environment;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;

/**
 * Abstract class that offers various File IO related methods.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class FileIO {
	/**
	 * The images directory path
	 */
	static final String imagesPath = "images/";

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
	 * Returns the path to the application's data.
	 *
	 * @return	the applications base directory (the "bold" directory)
	 */
	public static File getAppRootPath(){
		File path = new File(Environment.getExternalStorageDirectory(),
				appRootPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the path to the BOLD app's user directory.
	 *
	 */
	public static File getUsersPath(){
		File path = new File(getAppRootPath(), usersPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the path to the BOLD app's images directory.
	 *
	 * @return The path to the images directory
	 */
	public static File getImagesPath(){
		File path = new File(getAppRootPath(), imagesPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the path to the BOLD app's recordings directory.
	 *
	 * @return The path to the recordings directory
	 */
	public static File getRecordingsPath(){
		File path = new File(getAppRootPath(), recordingsPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Takes a file path (relative to the bold directory or absolute ) and some
	 * data and writes the data into the file in the bold directory in external
	 * storage.
	 *
	 * @param	path	The path to the file in which the data is to be
	 * written.
	 * @param	data	The data that is to be written to the file.
	 *
	 * @return	true if successful; false otherwise.
	 */
	
	public static boolean write(String path, String data) {
		File file;
		// If the path is absolute, use that path, otherwise make it relative
		// to the bold directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}
		try {
			FileUtils.writeStringToFile(file, data, Charsets.UTF_8);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Takes an absolute file path, some data and
	 * writes the data into the file in the bold directory in external storage.
	 *
	 * @param	path	The path to the file in which the data is to be
	 * written.
	 * @param	data	The data that is to be written to the file.
	 *
	 * @return	true if successful; false otherwise.
	 */

	public static boolean write(File path, String data) {
		try {
			FileUtils.writeStringToFile(path, data, Charsets.UTF_8);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Takes a file path (relative to the bold directory) and
	 * returns a string containing the file's contents.
	 *
	 * @param	path	The path to the file in which the data lies.
	 *
	 * @return	A string containing the file's contents; null if something went
	 * wrong.
	 */
	public static String read(String path) {
		File file;
		// If the path is absolute, use that path, otherwise make it relative
		// to the bold directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}
		try {
			return FileUtils.readFileToString(file, Charsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	} 

	/**
	 * Takes a file path (relative to the bold directory) and
	 * returns a string containing the file's contents.
	 *
	 * @param	path	The path to the file in which the data lies.
	 *
	 * @return	A string containing the file's contents; null if something went
	 * wrong.
	 */
	public static String read(File path) {
		try {
			return FileUtils.readFileToString(path, Charsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Method to load all the users from the users directory into an array of
	 * User objects in GlobalState.
	 */
	public static void loadUsers() {
		// Get an array of all the UUIDs from the "users" directory
		List<String> userUUIDs = Arrays.asList(getUsersPath().list());

		// Get the user data from the metadata.json files
		List<User> users = new ArrayList<User>();
		JSONParser parser = new JSONParser();
		for (String userUUID : userUUIDs) {
			String jsonStr = read(
					new File(getUsersPath(), userUUID + "/metadata.json"));
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				users.add( new User(
						UUID.fromString(jsonObj.get("uuid").toString()),
						jsonObj.get("name").toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		User[] usersArray = new User[users.size()];
		GlobalState.setUsers(users.toArray(usersArray));
	}


	/**
	 * Method to load all the recordings from the recordings directory into an
	 * array of Recording objects.
	 *
	 * @return An array of all the recordings as Recording objects.
	 */
	 /*
	 public static void loadRecordings() {
	 	//Get an array of all the UUIDs from the "recordings" directory
		File dir = new File(getAppRootPath() + getRecordingsPath());
		JSONFilenameFilter fnf = new JSONFilenameFilter();
		String[] recordingUuids = dir.list(fnf);

		Recording[] recordings = new Recording[recordingUuids.length];
		JSONParser parser = new JSONParser();
		for (int i=0; i < recordingUuids.length; i++) {
			String jsonStr = read(getRecordingsPath() + recordingUuids[i]);
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				UUID originalUUID = null;
				if (jsonObj.containsKey("originalUUID")) {
					originalUUID = UUID.fromString(
							jsonObj.get("originalUUID").toString());
				}
				recordings[i] = new Recording(
						UUID.fromString(jsonObj.get("uuid").toString()),
						GlobalState.getUserMap().get(UUID.fromString(
								jsonObj.get("creatorUUID").toString())),
						jsonObj.get("recording_name").toString(),
						new StandardDateFormat().parse(
								jsonObj.get("date_string").toString()),
						originalUUID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		GlobalState.setRecordings(recordings);
	 }
	 */

	/**
	 * Delete the filename supplied as an argument
	 *
	 * @param	fileName	Name of the file to be deleted.
	 */
	public static boolean delete(String fileName) {
		File file = new File(getAppRootPath() + fileName);
		return file.delete();
	}
}
