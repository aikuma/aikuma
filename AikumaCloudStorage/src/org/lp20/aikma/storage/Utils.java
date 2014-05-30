package org.lp20.aikma.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Utils {
	static public void copyStream(InputStream is, OutputStream os) throws IOException {
		copyStream(is, os, false);
	}
	
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
}
