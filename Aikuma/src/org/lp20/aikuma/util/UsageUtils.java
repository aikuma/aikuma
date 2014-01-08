/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.os.StatFs;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Provides information about the sdcard usage.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class UsageUtils {

	/**
	 * Returns the amount of bytes available in the specified directory.
	 *
	 * @param	dir	A File representing the directory
	 * @return	the number of bytes available for use.
	 */
	private static float bytesAvailable(File dir) {
		StatFs stat = new StatFs(dir.getPath());
		long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
		return bytesAvailable;
	}

	/**
	 * Returns the amount of bytes used by the directory (ie the size of the
	 * directory)
	 *
	 * @param	dir	A File representing the directory
	 * @param	fnf	A filenamefilter that specifies what types of files to
	 * consider
	 */
	private static float bytesUsed(File dir, FilenameFilter fnf) {
		if (dir.exists()) {
			long result = 0;
			File[] fileList = dir.listFiles(fnf);
			for(int i = 0; i < fileList.length; i++) {
				// Recursive call if it's a directory
				if(fileList[i].isDirectory()) {
					result += bytesUsed(fileList [i], fnf);
				} else {
					// Sum the file size in bytes
					result += fileList[i].length();
				}
			}
			return result; // return the file size
		}
		return 0;
	}

	/**
	 * Returns the number of seconds of recordings in the synced recordings
	 * directory.
	 *
	 * @param	sampleRate	The sampleRate of the recordings
	 * @param	sampleSize	The size of each sample in bits
	 */
	public static float secondsUsed(int sampleRate, int sampleSize) {
		float bytesUsed = bytesUsed(new File("/sdcard/aikuma/recordings"),
				new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						return filename.endsWith(".wav");
					}
				});
		//If the sample size isn't 16, it's assumed to be 8.
		return bytesUsed / ((sampleSize == 16 ? 2 : 1) * sampleRate);
	}

	/**
	 * Returns the number of seconds of recordings available

	/*
	public static double hoursRemaining(int sampleRate, int sampleSize) {
		
	}

	public static double hoursUsed(int sampleRate, int sampleSize) {
	}

	public static int numRecordings() {
	}

	public static int numRespeakings() {
	}
*/

}
