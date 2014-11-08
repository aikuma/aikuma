/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * Contains utilities to deal with images in the app.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public final class ImageUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ImageUtils() {}

	/**
	 * Returns the images directory.
	 *
	 * @return	A File representing the images directory.
	 */
	private static File getImagesPath() {
		File path = new File(FileIO.getAppRootPath(), "images");
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the directory for images that are not to be synced.
	 *
	 * @return	A File representing the no-sync images directory.
	 */
	private static File getNoSyncImagesPath() {
		File path = new File(FileIO.getNoSyncPath(), "images");
		path.mkdirs();
		return path;
	}

	/**
	 * Resizes the photo to the specified scale.
	 *
	 * @param	original	The original image
	 * @param	scale	the scale that should be applied to the original (0.5
	 * results in an image 50% the size of the original)
	 * @return	A Bitmap representing the resized image.
	 */
	public static Bitmap resizeBitmap(
			Bitmap original, float scale) {
		if (original == null) {
			throw new IllegalArgumentException("Can not resize null bitmap");
		}
		if (scale < 0) {
			throw new IllegalArgumentException(
					"scale argument cannot be less than zero");
		}
		int width = original.getWidth();
		int height = original.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resized = Bitmap.createBitmap(
				original, 0, 0, width, height, matrix, false);
		assert resized != null;
		return resized;
	}

	/**
	 * Method that retrieves image from file and returns a corresponding bitmap
	 * (rotates the file according to the EXIF orientation tag, if applicable).
	 *
	 * @param	path	The absolute path of the image file.
	 * @throws	IOException	If the file or Bitmap doesn't exist, or there is an
	 * I/O issue loading it.
	 * @return	The image as a bitmap
	 */
	public static Bitmap retrieveFromFile(String path) throws IOException {
		// If the path isn't specified absolutely, assume the aikuma directory.
		if (!path.startsWith("/")) {
			path = new File(getImagesPath(), path).getPath();
		}
		Bitmap bmp = BitmapFactory.decodeFile(path);
		if (bmp != null) {
			return bmp;
		} else {
			throw new IOException("No such bitmap file exists.");
		}
	}

	/**
	 * Returns the Image associated with a given speaker UUID.
	 *
	 * @param	uuid	The UUID of the speaker.
	 * @return	A File containing the speaker's image.
	 */
	public static File getImageFile(UUID uuid) {
		return new File(getImagesPath(), uuid.toString() + ".jpg");
	}

	/**
	 * Returns the Image associated with a given speaker UUID, looking in the
	 * no-sync directory.
	 *
	 * @param	uuid	The UUID of the speaker in question.
	 * @return	A File in the no-sync directory containing the image of the
	 * speaker.
	 */
	public static File getNoSyncImageFile(UUID uuid) {
		return new File(getNoSyncImagesPath(), uuid.toString() + ".jpg");
	}

	/**
	 * Returns the small Image associated with a given speaker UUID.
	 *
	 * @param	uuid	The UUID of the speaker.
	 * @return	A File containing the small version of the speaker's image.
	 */
	public static File getSmallImageFile(UUID uuid) {
		return new File(getImagesPath(), uuid.toString() + ".small.jpg");
	}

	/**
	 * Moves the images of the target user to the synced image directory.
	 *
	 * @param	uuid	The UUID of the user whose images are to be allowed to
	 * sync.
	 * @throws	IOException in the event of an I/O related exception when
	 * moving the file.
	 */
	public static void enableImageSync(UUID uuid) throws IOException {
		FileUtils.moveFileToDirectory(getNoSyncSmallImageFile(uuid),
				ImageUtils.getImagesPath(), false);
		FileUtils.moveFileToDirectory(getNoSyncImageFile(uuid),
				ImageUtils.getImagesPath(), false);
	}

	/**
	 * Returns the small Image associated with a given speaker UUID, looking in
	 * the no-sync directory.
	 *
	 * @param	uuid	The UUID of the speaker in question.
	 * @return	A File containing the small image of the speaker in the no-sync
	 * directory.
	 */
	public static File getNoSyncSmallImageFile(UUID uuid) {
		return new File(getNoSyncImagesPath(), uuid.toString() + ".small.jpg");
	}

	/**
	 * Returns a Bitmap associated with a given speaker UUID.
	 *
	 * @param	uuid	The UUID of the speaker.
	 * @throws	IOException	If an I/O exception arises when accessing the file.
	 * @return	A Bitmap of the speaker's image.
	 */
	public static Bitmap getImage(UUID uuid) throws IOException {
		File file = getImageFile(uuid);
		return retrieveFromFile(file);
	}

	/**
	 * Returns a small Bitmap associated with a given speaker UUID.
	 *
	 * @param	uuid	the UUID of the speaker
	 * @throws	IOException	If an I/O exception arises when accessing the file.
	 * @return	A Bitmap of the speaker's small image.
	 */
	public static Bitmap getSmallImage(UUID uuid) throws IOException {
		File file = getSmallImageFile(uuid);
		return retrieveFromFile(file);
	}

	/**
	 * Returns a small Bitmap associated with a given speaker UUID, looking on
	 * the no-sync directory.
	 *
	 * @param	uuid	The UUID of the Speaker whose image it is.
	 * @throws	IOException	if an I/O related exception is thrown when
	 * accessing the file.
	 * @return	A Bitmap representing the small image of a speaker stored in
	 * the no-sync directory.
	 */
	public static Bitmap getNoSyncSmallImage(UUID uuid) throws IOException {
		File file = getNoSyncSmallImageFile(uuid);
		return retrieveFromFile(file);
	}

	/**
	 * Creates a small version of a speaker image for use in Aikuma.
	 *
	 * @param	uuid	The uuid of the speaker
	 * @throws	IOException	if an I/O related exception is thrown when
	 * accessing the file
	 */
	public static void createSmallSpeakerImage(UUID uuid) throws IOException {
		String imageFilePath = getNoSyncImageFile(uuid).getPath();
		Bitmap image = BitmapFactory.decodeFile(imageFilePath);
		if (image == null) {
			throw new IOException("The image could not be decoded.");
		}
		int rotate = 0;
		
		//Check for EXIF TAG, and rotate if necessary.
		ExifInterface exif = new ExifInterface(imageFilePath);
		int orientation = exif.getAttributeInt(
				ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		}

		Bitmap small = ImageUtils.resizeBitmap(image, 0.1f);
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);

			small = Bitmap.createBitmap(small, 0, 0,
					small.getWidth(), small.getHeight(), matrix, true);
		}
		FileOutputStream out = new FileOutputStream(
				new File(getNoSyncImagesPath(), uuid.toString() + ".small.jpg"));
		small.compress(Bitmap.CompressFormat.JPEG, 100, out);
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
	public static Bitmap retrieveFromFile(File path) throws IOException {
		return retrieveFromFile(path.toString());
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
	public static void moveImageFileFromUri(Context context, 
			Uri srcUri, UUID outputUUID) throws IOException {
		File imageOutputFile = getNoSyncImageFile(outputUUID);
		
		InputStream fis = context.getContentResolver().
				openInputStream(srcUri);
		OutputStream fos = new FileOutputStream(imageOutputFile);
		
		byte buffer[] = new byte[1024];
		int length=0;
		while((length=fis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}
		
		fis.close();
		fos.close();
	}
	
	/**
	 * Return the number of pixels in the device corresponding to dp
	 * @param context	Application device context
	 * @param dp		The device independent pixels
	 * @return			The number of pixels calculated with device's scale and dp
	 */
	public static int getPixelsFromDp(Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int pixels = (int) (dp * scale + 0.5f);
		return pixels;
	}
}
