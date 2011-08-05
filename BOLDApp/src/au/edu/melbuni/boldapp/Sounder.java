package au.edu.melbuni.boldapp;


public class Sounder {

	protected String getBasePath() {
		return Bundler.getBasePath() + "recordings/";
	}

	protected String generateFullFilename(String relativeFilename) {
		String fileName = getBasePath();
		fileName += relativeFilename;
		fileName += ".3gp";
		return fileName;
	}

}