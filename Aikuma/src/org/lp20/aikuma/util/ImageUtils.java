package au.edu.unimelb.aikuma.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.File;
import java.io.IOException;

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
	 	
	 */
	public static Bitmap resizeBitmap(
			Bitmap original, float scale) throws IOException {
		if (original == null) {
			throw new IOException("Can not resize null bitmap");
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
		Bitmap image = BitmapFactory.decodeFile(path);
		if (image == null) {
			throw new IOException("The image could not be decoded.");
		}
		int rotate = 0;
		
		//Check for EXIF TAG, and rotate if necessary.
		try {
			ExifInterface exif = new ExifInterface(path);
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

		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);

			image = Bitmap.createBitmap(image, 0, 0,
					image.getWidth(), image.getHeight(), matrix, true);
		}

		assert image != null;
		return image;
	}

	/**
	 * Method that retrieves image from file and returns a corresponding bitmap
	 * (rotates the file according to the EXIF orientation tag, if applicable).
	 *
	 * @param	path	The absolute path of the image file.
	 * @return	The image as a bitmap
	 */
	public static Bitmap retrieveFromFile(File path) throws IOException {
		return retrieveFromFile(path.toString());
	}
}
