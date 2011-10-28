package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.persisters.Persister;


public class Sounder {

	protected String getBasePath() {
		// TODO 
		return Persister.getBasePath() + "recordings/";
	}

	protected String generateFullFilename(String relativeFilename) {
		String fileName = getBasePath();
		fileName += relativeFilename;
		fileName += ".3gp";
		return fileName;
	}

}