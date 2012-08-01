package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.os.Environment;
import android.util.Log;
import java.util.Scanner;
import java.io.FileInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.UUID;

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
			Log.e("CaughtExceptions", e.getMessage());
		}
	}

	/**
	 * Takes a file path relative to the bold directory in external storage and
	 * returns a string containing the file's contents.
	 *
	 * @param filePath The filename, including parent paths within the bold
	 * dir.
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
			Log.e("CaughtExceptions", e.getMessage());
		}
		Log.i("yoyoyo", "gaybacon" + text.toString());
		return text.toString();
	}

	/**
	 * Method to load all the users from the users directory into a list of
	 * User objects.
	 *
	 * @return An array of all the users as user objects
	 */
	public static User[] loadUsers() {
		// Get an array of all the UUIDs from the "users" directory
		File dir = new File(getAppRootPath() + "users");
		String[] userUuids = dir.list();

		// Get the user data from the metadata.json files
		User[] users = new User[userUuids.length];
		JSONParser parser = new JSONParser();
		for (int i=0; i < userUuids.length; i++) {
			String jsonStr = read("users/" + userUuids[i] + "/metadata.json");
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				users[i] = new User(
						UUID.fromString(jsonObj.get("uuid").toString()),
						jsonObj.get("name").toString());
			} catch (Exception e) {
				Log.e("GottaCatchEmAll", e.getMessage());
			}
		}
		return users;
	}
}
