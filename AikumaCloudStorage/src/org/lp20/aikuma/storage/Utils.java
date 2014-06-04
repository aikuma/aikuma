package org.lp20.aikuma.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * A collection of utility methods.
 * 
 * @author haejoong
 */
public class Utils {
	/**
	 * Copy data from an InputStream to an OutputStream.
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	static public void copyStream(InputStream is, OutputStream os) throws IOException {
		copyStream(is, os, false);
	}
	
	/**
	 * Copy data from an InputStream to an OutputStream. Close the OutputStream
	 * after copying if closeOs is set to {@code true}.
	 * 
	 * @param is
	 * @param os
	 * @param closeOs
	 * @throws IOException
	 */
	static public void copyStream(InputStream is, OutputStream os, boolean closeOs) throws IOException {
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf,  0, 8192)) != -1) {
			os.write(buf, 0, n);
		}
		os.flush();
		if (closeOs)
			os.close();
	}

	/**
	 * Read all data from the InputStream and turn it into a string.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	static public String readStream(InputStream is) throws IOException {
		BufferedReader in = new BufferedReader(
			new InputStreamReader(is)
		);
		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();		
	}
	
	/**
	 * Helps build a URL.
	 * 
	 * @author haejoong
	 */
	static class UrlBuilder {
		String query_;
		String joiner_;
		URL context_;
		
		/**
		 * Constructor a builder object with an initial URL.
		 * 
		 * @param base
		 * @throws MalformedURLException
		 */
		public UrlBuilder(String base) throws MalformedURLException {
			context_ = new URL(base);
			query_ = "";
			joiner_ = "?";
		}
		
		/**
		 * Append to or reset the path component of the current URL.
		 * @param path
		 * @throws MalformedURLException
		 */
		public void setPath(String path) throws MalformedURLException {
			context_ = new URL(context_, path);
		}
		
		/**
		 * Add a query element to the current URL.
		 * @param key
		 * @param value
		 * @throws UnsupportedEncodingException
		 */
		public void addQuery(String key, String value) throws UnsupportedEncodingException {
			String k = URLEncoder.encode(key, "UTF-8");
			String v = URLEncoder.encode(value, "UTF-8");
			query_ = joiner_ + k + "=" + v;
			joiner_ = "&";
		}
		
		/**
		 * Return the current URL.
		 * @return
		 * @throws MalformedURLException
		 */
		public URL toUrl() throws MalformedURLException {
			return new URL(context_, query_);
		}
	}
	
	/**
	 * Turns a set of key value pairs into a query string used to compose a
	 * url.
	 * 
	 * @param map Key value pairs
	 * @return Query string, or null if error occurs. 
	 */
	static public String map2query(Map<String,String> map) {
		String s = "";
		String joiner = "";
		for (String key: map.keySet()) {
			try {
				s += joiner + key + "=" + URLEncoder.encode(map.get(key), "UTF-8");
				joiner += "&";
			}
			catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		return s;
	}
}
