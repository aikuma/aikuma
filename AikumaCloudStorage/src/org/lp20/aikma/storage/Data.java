package org.lp20.aikma.storage;

import java.io.*;
import java.nio.file.Files;

/**
 * A container of the data to be uploaded to a DataStore.
 * 
 * @author haejoong
 */
public class Data {
	InputStream is_;
	String mimeType_;
	
	/**
	 * Construct the class with an InputStream and specified mime type.
	 * 
	 * @param is
	 * @param mimeType
	 */
	public Data(InputStream is, String mimeType) {
		is_ = is;
		mimeType_ = mimeType;
	}
	
	/**
	 * Construct the class with an InputStream. Mime type is set to
	 * application/octet-stream.
	 * 
	 * @param is
	 */
	public Data(InputStream is) {
		this(is, "application/octet-stream");
	}
	
	/**
	 * Factory function that construct the class using a File and specified
	 * mime type.
	 * 
	 * @param file
	 * @param mimeType
	 * @return
	 */
	public static Data fromFile(File file, String mimeType) {
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return new Data(is, mimeType);
	}
	
	/**
	 * Make a Data object with a File. Mime type is set to
	 * application/octet-stream.
	 * 
	 * @param file
	 * @return
	 */
	public static Data fromFile(File file) {
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return new Data(is);
	}
	
	/**
	 * Make a Data object with a File. Mime type is guessed from the
	 * File object.
	 * 
	 * @param file
	 * @return
	 */
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
	
	/**
	 * Make a Data object from the file name and mime type.
	 * 
	 * @param filename
	 * @param mimeType
	 * @return
	 */
	public static Data fromFileName(String filename, String mimeType) {
		File file = new File(filename);
		return fromFile(file, mimeType);
	}
	
	/**
	 * Make a Data object from the file name. Mime type is set to
	 * application/octet-stream.
	 * 
	 * @param filename
	 * @return
	 */
	public static Data fromFileName(String filename) {
		File file = new File(filename);
		return fromFile(file);
	}
	
	/**
	 * Make a Data object from the file name. Mime type is guessed.
	 * 
	 * @param filename
	 * @return
	 */
	public static Data fromFileName2(String filename) {
		File file = new File(filename);
		return fromFile2(file);
	}

	/**
	 * @return The current InputStream object.
	 */
	public InputStream getInputStream() {
		return is_;
	}
	
	/**
	 * @return The mime type of the InputStream object.
	 */
	public String getMimeType() {
		return mimeType_;
	}
}
