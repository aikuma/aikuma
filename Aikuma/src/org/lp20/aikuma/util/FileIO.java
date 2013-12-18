package org.lp20.aikuma.util;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.lp20.aikuma.R;
import com.google.common.base.Charsets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lp20.aikuma.model.Language;

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
	static final String APP_ROOT_PATH = "aikuma/";

	/**
	 * Returns the path to the application's data.
	 *
	 * @return	A File representing the applications base directory (the "aikuma"
	 * directory)
	 */
	public static File getAppRootPath(){
		File path = new File(Environment.getExternalStorageDirectory(),
				APP_ROOT_PATH);
		path.mkdirs();
		assert path != null;
		return path;
	}

	/**
	 * Takes a file path (relative to the aikuma directory or absolute) and some
	 * data and writes the data into the file in the aikuma directory in external
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
		// to the aikuma directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}
		FileUtils.writeStringToFile(file, data, Charsets.UTF_8);
	}

	/**
	 * Takes an absolute file path, some data and
	 * writes the data into the file in the aikuma directory in external storage.
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
	 * Takes a File representation of a path and a JSONObject and writes that
	 * JSONObject to the file.
	 *
	 * @param	path	The File where the JSONObject will be written.
	 * @param	jsonObj	The JSONObject to be written.
	 */
	public static void writeJSONObject(File path, JSONObject jsonObj)
			throws IOException {
		StringWriter stringWriter = new StringWriter();
		jsonObj.writeJSONString(stringWriter);
		String jsonStr = stringWriter.toString();
		write(path, jsonStr);
	}

	/**
	 * Takes a file path (relative to the aikuma directory) and
	 * returns a string containing the file's contents.
	 *
	 * @param	path	The path to the file in which the data lies.
	 *
	 * @return	A string containing the file's contents;
	 */
	public static String read(String path) throws IOException {
		File file;
		// If the path is absolute, use that path, otherwise make it relative
		// to the aikuma directory.
		if (path.startsWith("/")) {
			file = new File(path);
		} else {
			file = new File(getAppRootPath(), path);
		}

		return FileUtils.readFileToString(file, Charsets.UTF_8);
	} 

	/**
	 * Takes a file path (relative to the aikuma directory) and
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
	 * Takes a file path and reads a JSONObject from that file.
	 *
	 * @param	path	The path to the file where the JSON is stored.
	 */
	public static JSONObject readJSONObject(File path) throws IOException {
		try {
			String jsonStr = read(path);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			return jsonObj;
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Loads the ISO 639-3 language codes from the original text file.
	 *
	 * @param	resources	resources so that the iso 639-3 text file can be
	 * retrieved.
	 * @return	a map from language names to their corresponding codes.
	 */
	public static List<Language> readLangCodes(Resources resources) throws IOException {
		InputStream is = resources.openRawResource(R.raw.iso_639_3);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, Charsets.UTF_8);
		String inputString = writer.toString();
		List<Language> languages = new ArrayList<Language>();
		String[] lines = inputString.split("\n");
		for (int i = 1; i < lines.length; i++) {
			String[] elements = lines[i].split("(?=\t)");
			languages.add(new Language(elements[6].trim(), elements[0].trim()));
		}
		return languages;
	}

	public static void writeDefaultLanguages(List<Language> defaultLanguages)
			throws IOException  {
		CSVWriter writer = new CSVWriter(new FileWriter(
				new File(getAppRootPath(), "default_languages.csv")), '\t');
		String[] entry = new String[2];
		for (Language lang : defaultLanguages) {
			entry[0] = lang.getName();
			entry[1] = lang.getCode();
			writer.writeNext(entry);
		}
		writer.close();
	}

	public static List readDefaultLanguages() {
		List<Language> defaultLanguages = new ArrayList<Language>();
		try {
			CSVReader reader = new CSVReader(new FileReader(
					new File(getAppRootPath(), "default_languages.csv")), '\t');
			String[] nextLine;
			try {
				while ((nextLine = reader.readNext()) != null) {
					defaultLanguages.add(new Language(nextLine[0], nextLine[1]));
				}
			} catch (IOException e) {
				// Return as many languages as we have managed to retrieve.
				return defaultLanguages;
			}
		} catch (FileNotFoundException e) {
			// Return an empty language list.
			return defaultLanguages;
		}
		return defaultLanguages;
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
