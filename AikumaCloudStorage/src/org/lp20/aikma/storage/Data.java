package org.lp20.aikma.storage;

import java.io.*;
import java.nio.file.Files;

public class Data {
	InputStream is_;
	String mimeType_;
	
	public Data(InputStream is, String mimeType) {
		is_ = is;
		mimeType_ = mimeType;
	}
	
	public Data(InputStream is) {
		this(is, "application/octet-stream");
	}
	
	public static Data fromFile(File file, String mimeType) {
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return new Data(is, mimeType);
	}
	
	public static Data fromFile(File file) {
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return new Data(is);
	}
	
	public static Data fromFile2(File file) {
		String mimeType;
		try {
			mimeType = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			return null;
		}
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return new Data(is, mimeType);
	}
	
	public static Data fromFileName(String filename, String mimeType) {
		File file = new File(filename);
		return fromFile(file, mimeType);
	}

	public static Data fromFileName(String filename) {
		File file = new File(filename);
		return fromFile(file);
	}
	
	public static Data fromFileName2(String filename) {
		File file = new File(filename);
		return fromFile2(file);
	}

	public InputStream getInputStream() {
		return is_;
	}
	
	public String getMimeType() {
		return mimeType_;
	}
}
