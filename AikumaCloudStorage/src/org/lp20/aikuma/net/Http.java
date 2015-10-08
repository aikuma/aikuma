package org.lp20.aikuma.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.lp20.aikuma.storage.Utils;

public class Http {
	
	private HttpURLConnection con;

	private OutputStream writer;
	
	public static Http apply(URL url) { return new Http(url); }
	
	public static Http apply(String url) throws MalformedURLException { return new Http(new URL(url)); }

	public Http() {}
	
	public Http(URL url) {
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(true);
		} catch (IOException e) {
			con = null;
		}
	}
	
	public Http method(String method) throws ProtocolException {
		if(con != null)
			con.setRequestMethod(method);
		return this;
	}
	
	public Http header(String key, String value) {
		if(con != null) 
			con.setRequestProperty(key, value);
		return this;
	}
	
	public Http chunked(int num) {
		if(con != null)
			con.setChunkedStreamingMode(num);
		return this;
	}
	
	public Http body(String b) throws IOException {
		if(con != null) {
			setWriter();
			writer.write(b.getBytes());
		}
		
		return this;
	}
	
	public Http body(InputStream is) throws IOException {
		if(con != null) {
			setWriter();
			Utils.copyStream(is, writer);
		}
		
		return this;
	}
	
	private void setWriter() throws IOException {
		if(writer == null) {
			if(!con.getDoOutput())
				con.setDoOutput(true);
			writer = con.getOutputStream();
		}
	}
	
	public int code() {
		if(con != null) {
			try {
				return con.getResponseCode();
			} catch (IOException e) {
				return 1100;
			}
		}
		else 
			return 1000;
	}
	
	public String message() {
		if(con != null) {
			try {
				return con.getResponseMessage();
			} catch (IOException e) {
				return "io exception: " + e.getMessage();
			}
		}
		else
			return "no connection";
	}
	
	public String read() {
		if(con != null) {
			try {
				return Utils.readStream(con.getInputStream());
			} catch (IOException e) {
				return null;
			}
		}
		else
			return null;
	}
	
	public InputStream inputStream() throws IOException {
		if(con != null)
			return con.getInputStream();
		else
			return null;
	}
}