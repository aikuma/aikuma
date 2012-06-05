package au.edu.melbuni.boldapp;

import org.apache.commons.net.ntp.TimeStamp;

public class TimestampedFilename {

	/* Takes a filename with an extension and
	* injects a timestamp.
	* 
	* We use this to create unique files, e.g.
	* respeakings from the same original that need to be separated.
	* (If two respeakings from the same original have been done
	* in the same second, this collision needs to be resolved
	* manually)
	* 
	* Example:
	*   "hello.wav" becomes "hello-3456789012.wav"
	*/
	public static String getFilenameFor(String filename) {
		return filename.replaceFirst("(\\.\\w+)$", "-"
				+ TimeStamp.getCurrentTime().getSeconds() + "$1");
	}

}
