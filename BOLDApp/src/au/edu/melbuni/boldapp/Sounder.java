package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.persisters.Persister;


public class Sounder {

	public static String getBasePath() {
		return Persister.getBasePath() + "recordings/";
	}

	public static String generateFullFilename(String relativeFilename) {
		String fileName = getBasePath();
		fileName += relativeFilename;
		fileName += ".3gp";
		return fileName;
	}

}