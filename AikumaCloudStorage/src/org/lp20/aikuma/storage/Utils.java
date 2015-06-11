package org.lp20.aikuma.storage;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.lp20.aikuma.net.Http;
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
                char[] a = new char[1024];
		StringBuffer sb = new StringBuffer();
                int n = 0;
		while ((n = in.read(a)) > 0) {
			sb.append(new String(a, 0, n));
		}
		return sb.toString();		
	}

    public static String validateIndexMetadata(Map<String, String> metadata, boolean forInsert) {
        StringBuilder ret = new StringBuilder();

        for (String key : metadata.keySet()) {
            if (!FusionIndex.MetadataField.isValidName(key)) {
                ret.append("Unknown metadata field: " + key + "\n");
            }
        }
        for (FusionIndex.MetadataField f: FusionIndex.MetadataField.values()) {
            String name = f.getName();
            if (forInsert && f.isRequired( )&& !metadata.containsKey(name))
                ret.append("Missing required field " + name + "\n");
            if (f.getFormat() != null && metadata.containsKey(name)) {
                // TODO: this assumes all formats are date formats; it should probably be different
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(f.getFormat());
                    Date tmp = sdf.parse(metadata.get(name));
                } catch (ParseException e) {
                    ret.append("Invalid data format for " + name +
                                                       "; does not match" + f.getFormat() + "\n");
                }
            }
            if ("discourse_type".equals(name)) {
                for (String tmp : metadata.get(name).split(",")) {
                    if (!FusionIndex.DiscourseType.isValidName(tmp))
                        ret.append(metadata.get(name) + " is not a valid discourse_type\n");
                }
            }
        }
        return ret.toString();
    }

    /**
	 * Helps build a URL.
	 * 
	 * @author haejoong
	 */
	public static class UrlBuilder {
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
			query_ += joiner_ + k + "=" + v;
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
				joiner = "&";
			}
			catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		return s;
	}

    /**
     * Make an http connection that is common to all http requests used in this
     * class.
     *
     * @param url
     * @param method
     * @return
     * @throws IOException
     */
    public static HttpURLConnection gapi_connect(URL url, String method, String accessToken) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(true);
        if (method.equals("POST") || method.equals("PUT"))
        	con.setDoOutput(true);
        else
        	con.setDoOutput(false);
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        return con;
    }
}
