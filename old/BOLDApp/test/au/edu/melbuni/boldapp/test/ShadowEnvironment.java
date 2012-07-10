package au.edu.melbuni.boldapp.test;

import java.io.File;

import android.os.Environment;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(Environment.class)
public class ShadowEnvironment {
	
	public static class FileStub extends File {

		public FileStub(String pathname) {
			super(pathname);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 7248713449661500715L;

		public String getAbsolutePath() {
			return "/mnt/sdcard";
		}
		
	}
	
	public static File getExternalStorageDirectory() {
		return null;
	}
	
}
