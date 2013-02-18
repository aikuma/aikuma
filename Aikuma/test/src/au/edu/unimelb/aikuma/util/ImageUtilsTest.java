package au.edu.unimelb.aikuma.util;

import android.graphics.Bitmap;
import java.io.IOException;
import junit.framework.TestCase;

public class ImageUtilsTest extends TestCase {
	public void testRetrieveFromFile() {
		boolean caught = false;
		try {
			Bitmap bmp = ImageUtils.retrieveFromFile("nonexistant.bmp");
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
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
}
