package au.edu.melbuni.boldapp.filefilters;

import java.io.File;
import java.io.FileFilter;

public class PatternFileFilter implements FileFilter {

	String pattern;
	
	public PatternFileFilter(String pattern) {
		this.pattern = pattern;
	}
	
	@Override
	public boolean accept(File file) {
		return file.getName().matches(pattern);
	}

}
