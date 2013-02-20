package au.edu.unimelb.aikuma.util;

import android.content.res.Resources;
import android.os.Environment;
import au.edu.unimelb.aikuma.R;
import com.google.common.base.Charsets;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

/**
 * Utility class that offers various File IO related methods.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public final class FileIO {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileIO () {}

	/**
	 * The ISO 8601 date format used for parsing dates from JSON files.
	 */
	private static StandardDateFormat standardDateFormat =
			new StandardDateFormat();
	

	/**
	 * The application's top level path in the external storage.
	 */
	static final String appRootPath = "bold/";

	/**
	 * Returns the path to the application's data.
	 *
	 * @return	A File representing the applications base directory (the "bold"
	 * directory)
	 */
	public static File getAppRootPath(){
		File path = new File(Environment.getExternalStorageDirectory(),
				appRootPath);
		path.mkdirs();
		assert path != null;
		return path;
	}

	/**
	 * Returns the path to the BOLD app's recordings directory.
	 *
	 * @return The path to the recordings directory (absolute).
	 */
	/*
	public static File getRecordingsPath(){
		File path = new File(getAppRootPath(), recordingsPath);
		path.mkdirs();
		assert path != null;
		return path;
	}
	*/

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
	 * Writes the metadata of a recording to file.
	 *
	 * @param	recording	The recording metadata to be written to file.
	 */
	/*
	public static void writeRecording(Recording recording) throws IOException {
		JSONObject encodedRecording = encodeRecording(recording);
		write(new File(getRecordingsPath(), recording.getUUID().toString() + ".json"),
				encodedRecording.toString());
	}
	*/

	/**
	 * Encodes the recording as a JSONObject
	 */
	 /*
	public static JSONObject encodeRecording(Recording recording) {
		JSONObject obj = new JSONObject();
		JSONObject encodedUser = new JSONObject();
		if (recording.hasUUID()) {
			obj.put("uuid", recording.getUUID().toString());
		}
		if (recording.hasCreatorUUID()) {
			obj.put("creator_uuid", recording.getCreatorUUID().toString());
		}
		if (recording.hasName()) {
			obj.put("recording_name", recording.getName());
		}
		if (recording.hasDate()) {
			obj.put("date_string",
					standardDateFormat.format(recording.getDate()));
		}
		if (recording.hasLanguages()) {
			obj.put("languages", encodeLanguages(recording.getLanguages()));
		}
		if (recording.hasOriginalUUID()) {
			obj.put("original_uuid", recording.getOriginalUUID().toString());
		}
		return obj;
	}
	*/

	/**
	 * Reads the recording information from it's metadata file
	 *
	 * @param	file	The path to the recordings metadata file
	 */
	 /*
	public static Recording readRecording(File path) throws IOException {
		String jsonStr = FileIO.read(path);
		JSONParser parser = new JSONParser();

		Recording recording = new Recording();

		try {
			JSONObject jsonObj = (JSONObject) parser.parse(jsonStr);

			if (jsonObj.containsKey("uuid")) {
				recording.setUUID(
						UUID.fromString(jsonObj.get("uuid").toString()));
			} else {
				throw new IOException( "No UUID found in JSON file.");
			}
			if (jsonObj.containsKey("recording_name")) {
				recording.setName(jsonObj.get("recording_name").toString());
			}
			if (jsonObj.containsKey("creator_uuid")) {
				recording.setCreatorUUID(
						UUID.fromString(jsonObj.get("creator_uuid").toString()));
			} else {
				// Added for backwards compatibility with old key name
				if (jsonObj.containsKey("creatorUUID")) {
					recording.setCreatorUUID(
							UUID.fromString(jsonObj.get("creatorUUID").toString()));
				}
			}
			if (jsonObj.containsKey("original_uuid")) {
				recording.setOriginalUUID(
						UUID.fromString(jsonObj.get("original_uuid").toString()));
			} else {
				// Added for backwards compatibility with old key name
				if (jsonObj.containsKey("originalUUID")) {
					recording.setCreatorUUID(
							UUID.fromString(jsonObj.get("originalUUID").toString()));
				}
			}
			if (jsonObj.containsKey("date_string")) {
				recording.setDate(standardDateFormat.parse(
						jsonObj.get("date_string").toString()));
			}
			// Added for backwards compatibility with the model where there was
			//only one language
			if (jsonObj.containsKey("language_name")) {
				if (jsonObj.containsKey("language_code")) {
					recording.addLanguage(new Language(
							jsonObj.get("language_name").toString(),
							jsonObj.get("language_code").toString()));
				}
			}
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
			recording.setLanguages(languages);
		} catch (Exception e) {
			throw new IOException(e);
		}

		return recording;
	}
	*/

	/*
	public static Recording readRecording(UUID uuid) throws IOException {
		return readRecording(new File(FileIO.getRecordingsPath(),
				uuid.toString() + ".json"));
	}
	*/

	/**
	 * Reads the recordings from file.
	 *
	 * @return	recordings	A list of all the recordings in the bold directory;
	 * null if something went wrong.
	 */
	 /*
	public static List<Recording> readRecordings() {
		// Get an array of all the UUIDs from the "users" directory
		JSONFilenameFilter fnf = new JSONFilenameFilter();
		List<String> recordingFilenames =
				Arrays.asList(getRecordingsPath().list(fnf));

		// Get the user data from the metadata.json files
		List<Recording> recordings = new ArrayList<Recording>();
		for (String recordingFilename : recordingFilenames) {
			try {
				recordings.add(readRecording(new File(FileIO.getRecordingsPath(),
						recordingFilename)));
			} catch (IOException e) {
				// Couldn't read that recording because the JSON file wasn't
				// formatted well. Oh well, we just won't add it.
			}
		}

		return recordings;
	}
	*/

	/**
	 * Loads the ISO 639-3 language codes from the original text file.
	 *
	 * @param	resources	resources so that the iso 639-3 text file can be
	 * retrieved.
	 * @return	a map from language names to their corresponding codes.
	 */
	public static Map readLangCodes(Resources resources) throws IOException {
		InputStream is = resources.openRawResource(R.raw.iso_639_3);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, Charsets.UTF_8);
		String inputString = writer.toString();
		Map<String,String> map = new HashMap<String,String>();
		String[] lines = inputString.split("\n");
		for (int i = 1; i < lines.length; i++) {
			String[] elements = lines[i].split("(?=\t)");
			map.put(elements[6].trim(), elements[0].trim());
		}
		return map;
	}

	/**
	 * Reads the information about the server into a Server object, using the
	 * default location of server.json in the app's root directory.
	 */
	 /*
	public static Server readServer() throws IOException {
		return FileIO.readServer(
				new File(FileIO.getAppRootPath(), "server.json"));
	}
	*/

	/**
	 * Reads the information about the server into a Server object
	 */
	 /*
	public static Server readServer(File serverInfoFile) throws IOException {
		try {
			JSONParser parser = new JSONParser();
			String jsonStr = FileIO.read(serverInfoFile);
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			return new Server(jsonObj.get("ipaddress").toString(),
					jsonObj.get("username").toString(),
					jsonObj.get("password").toString());
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		}
	}
	*/

	/*
	public static void writeServer(Server server) throws IOException {
		FileIO.writeServer(server,
				new File(FileIO.getAppRootPath(), "server.json"));
	}
	*/

	/*
	public static void writeServer(Server server, File serverInfoFile) throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("ipaddress", server.getIPAddress());
		obj.put("username", server.getUsername());
		obj.put("password", server.getPassword());

		StringWriter stringWriter = new StringWriter();
		obj.writeJSONString(stringWriter);
		String jsonText = stringWriter.toString();
		FileIO.write(serverInfoFile, jsonText);
	}
	*/
}
