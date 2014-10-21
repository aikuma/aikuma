package org.lp20.aikuma.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.model.Recording;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

/**
 * Contains utilities to deal with videos in the app.
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public final class VideoUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private VideoUtils() {}

	/**
	 * Returns the videos directory.
	 *
	 * @return	A File representing the videos directory.
	 */
	private static File getVideosPath() {
		File path = new File(FileIO.getAppRootPath(), "videos");
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the directory for videoss that are not to be synced.
	 *
	 * @return	A File representing the no-sync Videos directory.
	 */
	private static File getNoSyncVideosPath() {
		return Recording.getNoSyncRecordingsPath();
	}

	/**
	 * Returns the Video associated with a given speaker UUID.
	 *
	 * @param	uuid	The UUID of the speaker.
	 * @return	A File containing the speaker's Video.
	 */
	public static File getVideoFile(UUID uuid) {
		return new File(getVideosPath(), uuid.toString() + ".mp4");
	}

	/**
	 * Returns the Video associated with a given speaker UUID, looking in the
	 * no-sync directory.
	 *
	 * @param	uuid	The UUID of the speaker in question.
	 * @return	A File in the no-sync directory containing the Video of the
	 * speaker.
	 */
	public static File getNoSyncVideoFile(UUID uuid) {
		return new File(getNoSyncVideosPath(), uuid.toString() + ".mp4");
	}


	/**
	 * Moves the Videos of the target user to the synced Video directory.
	 *
	 * @param	uuid	The UUID of the user whose Videos are to be allowed to
	 * sync.
	 * @throws	IOException in the event of an I/O related exception when
	 * moving the file.
	 */
	public static void enableVideoSync(UUID uuid) throws IOException {
		FileUtils.moveFileToDirectory(getNoSyncVideoFile(uuid),
				VideoUtils.getVideosPath(), false);
	}
	
	
	/**
	 * Copy the file from srcUri to the no-sync video directory
	 * 
	 * @param context		Activity calling this function 
	 * 						(used to get ContentResolver)
	 * @param srcUri		SourceFile's URI
	 * @param outputUUID	OutputFile's UUID(Filename)
	 * @throws IOException	IOException occurs while copying the file
	 */
	public static void moveVideoFileFromUri(Context context, 
			Uri srcUri, UUID outputUUID) throws IOException {
		File videoOutputFile = getNoSyncVideoFile(outputUUID);
		
		InputStream fis = context.getContentResolver().
				openInputStream(srcUri);
		OutputStream fos = new FileOutputStream(videoOutputFile);
		
		byte buffer[] = new byte[1024];
		int length=0;
		while((length=fis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}
		
		fis.close();
		fos.close();
	}


	/**
	 * Method that retrieves image from file and returns a corresponding bitmap
	 * (rotates the file according to the EXIF orientation tag, if applicable).
	 *
	 * @param	path	The absolute path of the image file.
	 * @throws	IOException	If some I/O exception occurs when accessing
	 * the file
	 * @return	a Bitmap of the specified file.
	 */
//	public static Bitmap retrieveFromFile(File path) throws IOException {
//		return retrieveFromFile(path.toString());
//	}
}