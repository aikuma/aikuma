package au.edu.melbuni.boldapp;

import java.io.File;
import java.io.FileFilter;

public class UUIDFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return file.getName().matches(".{8}-.{4}-.{4}-.{4}-.{12}.json");
	}

}
