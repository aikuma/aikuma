/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.os.StatFs;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.lp20.aikuma.model.Recording;

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
	 * @param	sampleRate	The sample rate of the recordings
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
	 * Returns the number of seconds of recordings available.
	 *
	 * @param	sampleRate	The sample rate of hte recordings
	 * @param	sampleSize	The size of each sample in bits.
	 */
	public static float secondsAvailable(int sampleRate, int sampleSize) {
		float bytesUsed = bytesAvailable(new File("/sdcard"));
		//If the sample size isn't 16, it's assumed to be 8.
		return bytesUsed / ((sampleSize == 16 ? 2 : 1) * sampleRate);
	}

	
	/**
	 * Returns the number of hours worth of recordings in the synced recordings
	 * directory.
	 *
	 * @param	sampleRate	The sample rate of the recordings
	 * @param	sampleSize	The size of each sample in bits
	 */
	public static float hoursUsed(int sampleRate, int sampleSize) {
		return secondsUsed(sampleRate, sampleSize) / 3600;
	}
	/**
	 * Returns the number of hours worth of recordings available.
	 *
	 * @param	sampleRate	The sample rate of hte recordings
	 * @param	sampleSize	The size of each sample in bits.
	 */
	public static float hoursAvailable(int sampleRate, int sampleSize) {
		return secondsAvailable(sampleRate, sampleSize) / 3600;
	}

	/**
	 * Returns the number of original recordings.
	 *
	 * @return	the number of original recordings in the aikuma directory.
	 */
	public static int numOriginals() {
		List<Recording> recordings = Recording.readAll();
		int originalCount = 0;
		for (Recording recording : recordings) {
			if (recording.isOriginal()) {
				originalCount++;
			}
		}
		return originalCount;
	}

	/**
	 * Returns the number of commentaries.
	 *
	 * @return	the number of commentaries in the aikuma directory.
	 */
	public static int numCommentaries() {
		List<Recording> recordings = Recording.readAll();
		int commentaryCount = 0;
		for (Recording recording : recordings) {
			if (!recording.isOriginal()) {
				commentaryCount++;
			}
		}
		return commentaryCount;
	}
}
