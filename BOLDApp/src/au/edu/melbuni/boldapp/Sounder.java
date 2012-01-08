package au.edu.melbuni.boldapp;

import java.io.File;
import java.io.IOException;

public class Sounder {

	public static String prepareFile(String fileName) {
		File file = new File(fileName + ".3gp");
		File parentFile = new File(file.getParent());
		parentFile.mkdirs();
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}