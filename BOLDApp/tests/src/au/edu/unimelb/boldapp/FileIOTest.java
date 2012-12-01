package au.edu.unimelb.boldapp;

import java.io.File;

import android.util.Log;

import junit.framework.TestCase;

public class FileIOTest extends TestCase {

	public void testGetAppRootPath() throws Exception {
		assertEquals(new File("/mnt/sdcard/bold/"), FileIO.getAppRootPath());
		Log.i("FileIO", FileIO.getAppRootPath().getAbsolutePath());
		Log.i("FileIO", FileIO.getAppRootPath().getCanonicalPath());
	}

}
