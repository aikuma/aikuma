package au.edu.melbuni.boldapp;

import org.apache.commons.net.ntp.TimeStamp;

public class TimestampedFilename {

	public static String getFilenameFor(String filename) {
		return filename.replaceFirst("(\\.\\w+)$", "-"
				+ TimeStamp.getCurrentTime().getSeconds() + "$1");
	}

}
