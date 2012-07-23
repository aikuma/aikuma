package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.os.Environment;
import android.util.Log;
import java.util.Scanner;
import java.io.FileInputStream;

public abstract class FileIO {
	static final String appRootPath = "bold/";

	public static String getAppRootPath(){
		File external = Environment.getExternalStorageDirectory();
		String path = external.getAbsolutePath() + "/" + appRootPath;
		return path;
	}

	/**
	Takes a file path and some data and writes the data into the file in the
	bold directory in external storage.

	@param filePath The filename, including parent paths within the bold dir.
	@param data The data that is to be written to the file.

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
	Takes a file path relative to the bold directory in external storage and
	returns a string containing the file's contents.

	@param filePath The filename, including parent paths within the bold dir.
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
}
