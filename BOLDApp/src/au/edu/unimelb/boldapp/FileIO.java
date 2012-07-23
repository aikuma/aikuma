package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.os.Environment;
import android.util.Log;

public abstract class FileIO {
	static final String appRootPath = "bold/";

	/**
	Takes a file name and some data and writes the data into the file in the
	bold directory in external storage.

	@param fileName The filename, including parent paths within the bold dir.
	@param data The data that is to be written to the file.

	*/
	public static void write(String fileName, String data) {
		try {
			File external = Environment.getExternalStorageDirectory();
			String path = external.getAbsolutePath() + "/" + appRootPath +
					fileName;
			File file = new File(path);
			file.getParentFile().mkdirs();
			BufferedWriter bos = new BufferedWriter(new FileWriter(path));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("yoyoyo", data);
		}
	}
}
