package au.edu.unimelb.boldapp;

import java.io.FilenameFilter;
import java.io.File;

/**
 * accepts only files with JSON extension
 */
public class JSONFilenameFilter implements FilenameFilter {
	/**
	 * The accept method that must be implemented.
	 *
	 * @return true if and only if the file has is suffixed by .json
	 */
	 public boolean accept(File dir, String name) {
		return name.matches("(.*)(\\.)(json)");
	 }
}
