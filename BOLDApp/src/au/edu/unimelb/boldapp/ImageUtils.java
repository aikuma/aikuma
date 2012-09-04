package au.edu.unimelb.boldapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

/**
 * Contains utilities to deal with images in the app.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ImageUtils {
	/**
	 * Method that retrieves image from file and returns a corresponding bitmap
	 * (rotates the file according to the EXIF orientation tag, if applicable).
	 *
	 * @param	path	The absolute path of the image file.
	 * @return	The image as a bitmap
	 */
	public static Bitmap retrieveFromFile(String path) {
		Bitmap image = BitmapFactory.decodeFile(path);
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

		return image;
	}
}
