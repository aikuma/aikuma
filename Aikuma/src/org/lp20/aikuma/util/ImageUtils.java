/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

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
	 */
	public static File getImageFile(UUID uuid) {
		return new File(getImagesPath(), uuid.toString() + ".jpg");
	}

	/**
	 * Returns the small Image associated with a given speaker UUID.
	 */
	public static File getSmallImageFile(UUID uuid) {
		return new File(getImagesPath(), uuid.toString() + ".small.jpg");
	}

	/**
	 * Returns a Bitmap associated with a given speaker UUID.
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
	 * @return	A Bitmap representing the small image.
	 */
	public static Bitmap getSmallImage(UUID uuid) throws IOException {
		File file = getSmallImageFile(uuid);
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
		String imageFilePath = getImageFile(uuid).getPath();
		Bitmap image = BitmapFactory.decodeFile(imageFilePath);
		if (image == null) {
			throw new IOException("The image could not be decoded.");
		}
		int rotate = 0;
		
		//Check for EXIF TAG, and rotate if necessary.
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		Bitmap small = ImageUtils.resizeBitmap(image, 0.05f);
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);

			small = Bitmap.createBitmap(small, 0, 0,
					small.getWidth(), small.getHeight(), matrix, true);
		}
		FileOutputStream out = new FileOutputStream(
				new File(getImagesPath(), uuid.toString() + ".small.jpg"));
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
}
