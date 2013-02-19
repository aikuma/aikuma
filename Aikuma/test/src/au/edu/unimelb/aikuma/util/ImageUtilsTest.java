package au.edu.unimelb.aikuma.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import au.edu.unimelb.aikuma.R;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import android.test.AndroidTestCase;

public class ImageUtilsTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		Bitmap bmp = BitmapFactory.decodeResource(
				getContext().getResources(), R.raw.image1);
		assertTrue(bmp != null);
		File dest = new File(FileIO.getAppRootPath(),
				"images/image1.jpg");
		FileOutputStream fos = new FileOutputStream(dest);
		assertTrue(bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos));
		fos.flush();
		fos.close();
	}

	public void testResizeBitmap() {
		Bitmap bmp = null;
		boolean caught = false;
		try {
			ImageUtils.resizeBitmap(bmp, 0.8f);
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	public void testRetrieveFromFile1() {
		boolean caught = false;
		try {
			Bitmap bmp = ImageUtils.retrieveFromFile("nonexistant.bmp");
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	public void testRetrieveFromFileAndResize() throws Exception {
		Bitmap bmp = ImageUtils.retrieveFromFile("image1.jpg");
		assertTrue(bmp != null);
		Bitmap resized = ImageUtils.resizeBitmap(bmp, 0.10f);
		File dest = new File(FileIO.getAppRootPath(),
				"images/image1resized.jpg");
		FileOutputStream fos = new FileOutputStream(dest);
		assertTrue(resized.compress(Bitmap.CompressFormat.JPEG, 100, fos));
		fos.flush();
		fos.close();
	}

	protected void tearDown() {
		new File(FileIO.getAppRootPath(), "images/image1.jpg").delete();
		new File(FileIO.getAppRootPath(), "images/image1resized.jpg").delete();
	}
}
