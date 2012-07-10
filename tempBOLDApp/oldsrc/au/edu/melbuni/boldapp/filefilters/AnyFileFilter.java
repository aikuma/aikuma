package au.edu.melbuni.boldapp.filefilters;

import java.io.File;
import java.io.FileFilter;

public class AnyFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return file.getName().matches(".+\\.json");
	}

}
