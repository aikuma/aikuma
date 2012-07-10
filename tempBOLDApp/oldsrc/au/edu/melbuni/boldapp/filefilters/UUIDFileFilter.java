package au.edu.melbuni.boldapp.filefilters;

import java.io.File;
import java.io.FileFilter;

public class UUIDFileFilter implements FileFilter {
	
	String extension;
	
	public UUIDFileFilter() {
		this("\\.json"); // TODO Make empty String default.
	}
	
	public UUIDFileFilter(String extension) {
		this.extension = extension;
	}
	
	@Override
	public boolean accept(File file) {
		return file.getName().matches(".{8}-.{4}-.{4}-.{4}-.{12}" + extension);
	}

}
