package au.edu.unimelb.boldapp;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		assert path != null;
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
		assert path != null;
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
		assert path != null;
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
		assert path != null;
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

	public static JSONObject encodeLanguage(Language language) {
		JSONObject encodedLanguage = new JSONObject();
		encodedLanguage.put("name", language.getName());
		encodedLanguage.put("code", language.getCode());
		return encodedLanguage;
	}

	public static JSONArray encodeLanguages(List<Language> languages) {
		JSONArray languageArray = new JSONArray();
		for (Language language : languages) {
			languageArray.add(encodeLanguage(language));
		}
		return languageArray;
	}

	public static JSONObject encodeUser(User user) {
		JSONObject encodedUser = new JSONObject();
		encodedUser.put("name", user.getName());
		encodedUser.put("uuid", user.getUUID().toString());
		encodedUser.put("languages", encodeLanguages(user.getLanguages()));
		return encodedUser;
	}

	/**
	 * Store user information to file
	 *
	 * @param	username	The name of the user.
	 * @param	uuid	The users associated UUID.
	 *
	 */
	public static void writeUser(String name, UUID uuid,
			List<Language> languages) throws IOException {

		// Create the JSON object
		JSONObject encodedUser = encodeUser(new User(uuid, name, languages));

		// Write the JSON object to file.
		StringWriter stringWriter = new StringWriter();
		encodedUser.writeJSONString(stringWriter);
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
		writeUser(user.getName(), user.getUUID(), user.getLanguages());
		JSONObject encodedUser = encodeUser(user);
		write(new File(getUsersPath(), user.getUUID().toString() + "/metadata.json"),
				encodedUser.toString());
	}

	public static User readUser(String uuidString) throws IOException {
		User user;
		try {
			JSONParser parser = new JSONParser();
			String jsonStr = read(
					new File(getUsersPath(), uuidString + "/metadata.json"));
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			JSONArray languagesArray = (JSONArray) jsonObj.get("languages");
			List<Language> languages = new ArrayList<Language>();
			if (languagesArray != null) {
				for (Object langObj : languagesArray) {
					JSONObject jsonLangObj = (JSONObject) langObj;
					Language lang = new Language(
							jsonLangObj.get("name").toString(),
							jsonLangObj.get("code").toString());
					languages.add(lang);
				}
			}
			user = new User(
					UUID.fromString(jsonObj.get("uuid").toString()),
					jsonObj.get("name").toString(), languages);
			Log.i("readUser", "loaded " + user.getLanguages().toString());
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		}
		return user;
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
		for (String userUUID : userUUIDs) {
			users.add(readUser(userUUID));
		}

		return users;
	}

	/**
	 * Writes the metadata of a recording to file.
	 *
	 * @param	recording	The recording metadata to be written to file.
	 */
	public static void writeRecording(Recording recording) 
			throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("uuid", recording.getUUID().toString());
		obj.put("creatorUUID", recording.getCreatorUUID().toString());
		obj.put("recording_name", recording.getName());
		obj.put("date_string", new StandardDateFormat().format(recording.getDate()));
		Log.i("lol", " " + recording.getLanguage());
		obj.put("language_name", recording.getLanguage().getName());
		obj.put("language_code", recording.getLanguage().getCode());
		if (recording.getOriginalUUID() != null) {
			obj.put("originalUUID", recording.getOriginalUUID().toString());
		}
		StringWriter stringWriter = new StringWriter();
		obj.writeJSONString(stringWriter);
		String jsonText = stringWriter.toString();
		FileIO.write(new File(FileIO.getRecordingsPath(),
				recording.getUUID() + ".json"), jsonText);
	}

	public static Recording readRecording(File path) throws IOException {
		String jsonStr = FileIO.read(path);
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			UUID originalUUID;
			Language language;
			if (jsonObj.containsKey("originalUUID")) {
				originalUUID = UUID.fromString(
						jsonObj.get("originalUUID").toString());
			} else {
				originalUUID = null;
			}
			if (jsonObj.containsKey("language_name") &&
					jsonObj.containsKey("language_code")) {
				language = new Language((String)jsonObj.get("language_name"),
							(String)jsonObj.get("language_code"));
			} else {
				language = null;
			}
			return new Recording(
					UUID.fromString(jsonObj.get("uuid").toString()),
					UUID.fromString(jsonObj.get("creatorUUID").toString()),
					jsonObj.get("recording_name").toString(),
					new StandardDateFormat().parse(
							jsonObj.get("date_string").toString()),
					language,
					originalUUID);
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		} catch (java.text.ParseException e) {
			throw new IOException(e);
		}
	}

	public static Recording readRecording(UUID uuid) throws IOException {
		return readRecording(new File(FileIO.getRecordingsPath(),
				uuid.toString() + ".json"));
	}

	/**
	 * Reads the recordings from file.
	 *
	 * @return	recordings	A list of all the recordings in the bold directory;
	 * null if something went wrong.
	 */
	public static List<Recording> readRecordings() throws IOException {
		// Get an array of all the UUIDs from the "users" directory
		JSONFilenameFilter fnf = new JSONFilenameFilter();
		List<String> recordingFilenames =
				Arrays.asList(getRecordingsPath().list(fnf));

		// Get the user data from the metadata.json files
		List<Recording> recordings = new ArrayList<Recording>();
		for (String recordingFilename : recordingFilenames) {
			recordings.add(readRecording(new File(FileIO.getRecordingsPath(),
					recordingFilename)));
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

	/**
	 * Loads the ISO 639-3 language codes from the original text file.
	 *
	 * @param	resources	resources so that the iso 639-3 text file can be
	 * retrieved.
	 * @return	a map from language names to their corresponding codes.
	 */
	public static Map readLangCodes(Resources resources) throws IOException {
		InputStream is =
				resources.openRawResource(R.raw.iso_639_3);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, Charsets.UTF_8);
		String inputString = writer.toString();
		Map<String,String> map = new HashMap<String,String>();
		String[] lines = inputString.split("\n");
		for (String line : lines) {
			String[] elements = line.split("(?=\t)");
			map.put(elements[6].trim(), elements[0].trim());
		}
		return map;
	}
}
