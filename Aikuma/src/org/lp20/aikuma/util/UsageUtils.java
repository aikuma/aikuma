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
	 * @return	A float representing the number of bytes used by a directory.
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
	 * @return	A float representing the number of seconds used.
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
	 * @return	A float representing the number of seconds available.
	 */
	public static float secondsAvailable(int sampleRate, int sampleSize) {
		float bytesUsed = bytesAvailable(new File("/sdcard"));
		//If the sample size isn't 16, it's assumed to be 8.
		return bytesUsed / ((sampleSize == 16 ? 2 : 1) * sampleRate);
	}

	/**
	 * Returns a nicely formatted string describing how much time has been used
	 * (in an <Hours>h <minutes>m <seconds>s format).
	 *
	 * @param	sampleRate	The sample rate of the recordings
	 * @param	sampleSize	The size of each sample in bits.
	 * @return	A String describing the mount of recording time already used.
	 */
	public static String timeUsed(int sampleRate, int sampleSize) {
		long secondsUsed = (long) secondsUsed(sampleRate, sampleSize);
		return formatSeconds(secondsUsed);
	}


	/**
	 * Returns a nicely formatted string describing how much recording time is
	 * available (in an <Hours>h <minutes>m <seconds>s format).
	 *
	 * @param	sampleRate	The sample rate of the recordings
	 * @param	sampleSize	The size of each sample in bits.
	 * @return	A String describing the mount of recording time available.
	 */
	public static String timeAvailable(int sampleRate, int sampleSize) {
		long secondsAvailable = (long) secondsAvailable(sampleRate, sampleSize);
		return formatSeconds(secondsAvailable);
	}

	/**
	 * Formats a duration represented as seconds into a nicer <Hours>h
	 * <minutes>m <seconds>s format.
	 *
	 * @param	seconds	A long representing the amount of seconds.
	 * @return	A string describing the duration.
	 */
	private static String formatSeconds(long seconds) {
		int hours = (int) seconds / 3600;
		int remainder = (int) seconds - hours * 3600;
		int mins = remainder / 60;
		remainder = remainder - mins * 60;
		int secs = remainder;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(hours).append("h ");
		stringBuilder.append(mins).append("m");
		return stringBuilder.toString();
	}
	
	/**
	 * Return a formatted string of space
	 * @param space	The size in Bytes.
	 * @return	the formatted string of space
	 */
	public static String getStorageFormat(long space) {
		double convertedSpace = (double) space;
		int count = 0;
		String unit = "KB";
		while(convertedSpace > 100 && count < 3) {
			convertedSpace /= 1000;
			count++;
		}
		switch(count) {
		case 2:
			unit = "MB";
			break;
		case 3:
			unit = "GB";
			break;
		}
		
		return String.format("%5.2f%s", convertedSpace, unit);
	}
	
	/**
	 * Return a formatted string of time converted from space
	 * @param sampleRate	The sample rate of the recordings
	 * @param sampleSize	The size of each sample in bits.
	 * @param space			The size to be converted in Bytes.
	 * @return	the formatted string of time
	 */
	public static String getTimeFormat(
			int sampleRate, int sampleSize, long space) {
		long convertedSecs = space / ((sampleSize == 16 ? 2 : 1) * sampleRate);
		return formatSeconds(convertedSecs);
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
