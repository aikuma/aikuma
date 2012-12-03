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
import java.io.StringWriter;
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
import org.json.simple.parser.ParseException;

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
	 * @return	the path to the users directory (absolute).
	 */
	public static File getUsersPath(){
		File path = new File(getAppRootPath(), usersPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the path to the BOLD app's images directory.
	 *
	 * @return The path to the images directory (absolute).
	 */
	public static File getImagesPath(){
		File path = new File(getAppRootPath(), imagesPath);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the path to the BOLD app's recordings directory.
	 *
	 * @return The path to the recordings directory (absolute).
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
	 */
	
	public static void write(String path, String data) throws IOException {
		File file;
		// If the path is absolute, use that path, otherwise make it relative
		// to the bold directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}
		FileUtils.writeStringToFile(file, data, Charsets.UTF_8);
	}

	/**
	 * Takes an absolute file path, some data and
	 * writes the data into the file in the bold directory in external storage.
	 *
	 * @param	path	The path to the file in which the data is to be
	 * written.
	 * @param	data	The data that is to be written to the file.
	 *
	 */

	public static void write(File path, String data) throws IOException {
		FileUtils.writeStringToFile(path, data, Charsets.UTF_8);
	}

	/**
	 * Takes a file path (relative to the bold directory) and
	 * returns a string containing the file's contents.
	 *
	 * @param	path	The path to the file in which the data lies.
	 *
	 * @return	A string containing the file's contents;
	 */
	public static String read(String path) throws IOException {
		File file;
		// If the path is absolute, use that path, otherwise make it relative
		// to the bold directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}

		return FileUtils.readFileToString(file, Charsets.UTF_8);
	} 

	/**
	 * Takes a file path (relative to the bold directory) and
	 * returns a string containing the file's contents.
	 *
	 * @param	path	The path to the file in which the data lies.
	 *
	 * @return	A string containing the file's contents;
	 */
	public static String read(File path) throws IOException {
		return FileUtils.readFileToString(path, Charsets.UTF_8);
	}

	/**
	 * Store user information to file
	 *
	 * @param	username	The name of the user.
	 * @param	uuid	The users associated UUID.
	 *
	 */
	public static void writeUser(String name, UUID uuid) throws IOException {

		// Create the JSON object
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("uuid", uuid.toString());

		// Write the JSON object to file.
		StringWriter stringWriter = new StringWriter();
		obj.writeJSONString(stringWriter);
		String jsonText = stringWriter.toString();
		write(new File(getUsersPath(), uuid.toString() + "/metadata.json"),
				jsonText);

	}

	/**
	 * Store user information to file
	 *
	 * @param	user	The user to be written.
	 *
	 */
	public static void writeUser(User user) throws IOException {
		writeUser(user.getName(), user.getUUID());
	}

	/**
	 * Method to load all the users from the users directory into an array of
	 * User objects in GlobalState.
	 *
	 * @return	a list of users in the directory.
	 */
	public static List<User> readUsers() throws IOException {
		// Get an array of all the UUIDs from the "users" directory
		List<String> userUUIDs = Arrays.asList(getUsersPath().list());

		// Get the user data from the metadata.json files
		List<User> users = new ArrayList<User>();
		JSONParser parser = new JSONParser();
		for (String userUUID : userUUIDs) {
			try {
				String jsonStr = read(
						new File(getUsersPath(), userUUID + "/metadata.json"));
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				users.add( new User(
						UUID.fromString(jsonObj.get("uuid").toString()),
						jsonObj.get("name").toString()));
			} catch (org.json.simple.parser.ParseException e) {
				throw new IOException(e);
			}
		}

		//User[] usersArray = new User[users.size()];
		//GlobalState.setUsers(users.toArray(usersArray));

		return users;
	}

	/**
	 * DEPRECATED: Method to load all the users from the users directory into an array of
	 * User objects in GlobalState.
	 */
	public static void loadUsers() {
		// Get an array of all the UUIDs from the "users" directory
		List<String> userUUIDs = Arrays.asList(getUsersPath().list());

		// Get the user data from the metadata.json files
		List<User> users = new ArrayList<User>();
		JSONParser parser = new JSONParser();
		for (String userUUID : userUUIDs) {
			try {
				String jsonStr = read(
						new File(getUsersPath(), userUUID + "/metadata.json"));
				try {
					Object obj = parser.parse(jsonStr);
					JSONObject jsonObj = (JSONObject) obj;
					users.add( new User(
							UUID.fromString(jsonObj.get("uuid").toString()),
							jsonObj.get("name").toString()));
				} catch (ParseException e) {
				}
			} catch (IOException e) {
			}
		}

		User[] usersArray = new User[users.size()];
		GlobalState.setUsers(users.toArray(usersArray));

	}

	/**
	 * Writes the metadata of a recording to file.
	 *
	 * @param	recording	The recording metadata to be written to file.
	 */
	public static void writeRecordingMeta(Recording recording) 
			throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("uuid", recording.getUUID().toString());
		obj.put("creatorUUID", recording.getCreatorUUID().toString());
		obj.put("recording_name", recording.getName());
		obj.put("date_string", new StandardDateFormat().format(recording.getDate()));
		if (recording.getOriginalUUID() != null) {
			obj.put("originalUUID", recording.getOriginalUUID().toString());
		}
		StringWriter stringWriter = new StringWriter();
		obj.writeJSONString(stringWriter);
		String jsonText = stringWriter.toString();
		FileIO.write(new File(FileIO.getRecordingsPath(),
				recording.getUUID() + ".json"), jsonText);
	}

	/**
	 * Reads the recordings from file.
	 *
	 * @return	recordings	A list of all the recordings in the bold directory;
	 * null if something went wrong.
	 */
	public static List<Recording> readRecordingsMeta()
			throws IOException {
		// Get an array of all the UUIDs from the "users" directory
		JSONFilenameFilter fnf = new JSONFilenameFilter();
		List<String> recordingFilenames =
				Arrays.asList(getRecordingsPath().list(fnf));

		// Get the user data from the metadata.json files
		List<Recording> recordings = new ArrayList<Recording>();
		JSONParser parser = new JSONParser();
		for (String recordingFilename : recordingFilenames) {
			try {
				String jsonStr = read(
						new File(getRecordingsPath(), recordingFilename));
					Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				UUID originalUUID;
				if (jsonObj.containsKey("originalUUID")) {
					originalUUID = UUID.fromString(
							jsonObj.get("originalUUID").toString());
				} else {
					originalUUID = null;
				}

				recordings.add(new Recording(
						UUID.fromString(jsonObj.get("uuid").toString()),
						UUID.fromString(jsonObj.get("creatorUUID").toString()),
						jsonObj.get("recording_name").toString(),
						new StandardDateFormat().parse(
								jsonObj.get("date_string").toString()),
						originalUUID));
			} catch (org.json.simple.parser.ParseException e) {
				throw new IOException(e);
			} catch (java.text.ParseException e) {
				throw new IOException(e);
			}
		}

		return recordings;
	 }

	/**
	 * DEPRECATED: Delete the filename supplied as an argument
	 *
	 * @param	fileName	Name of the file to be deleted.
	 */
	public static boolean delete(String fileName) {
		File file = new File(getAppRootPath() + fileName);
		return file.delete();
	}
}
