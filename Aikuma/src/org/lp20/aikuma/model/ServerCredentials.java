package org.lp20.aikuma.model;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.lp20.aikuma.util.FileIO;

public class ServerCredentials {
	
	public ServerCredentials(String ipAddress, String username, String password) {
		setIPAddress(ipAddress);
		setUsername(username);
		setPassword(password);
	}

	public void write() throws IOException {
		JSONObject encodedServerCredentials = this.encode();
		FileIO.writeJSONObject(
				new File(FileIO.getAppRootPath(), "server_credentials.json"),
				encodedServerCredentials);
	}

	public static ServerCredentials read() throws IOException {
		JSONObject encodedServerCredentials = FileIO.readJSONObject(
				new File(FileIO.getAppRootPath(), "server_credentials.json"));
		String ipAddress = (String) encodedServerCredentials.get("ipAddress");
		String username = (String) encodedServerCredentials.get("username");
		String password = (String) encodedServerCredentials.get("password");
		return new ServerCredentials(ipAddress, username, password);
	}

	private JSONObject encode() {
		JSONObject encodedServerCredentials = new JSONObject();
		encodedServerCredentials.put("ipAddress", getIPAddress());
		encodedServerCredentials.put("username", getUsername());
		encodedServerCredentials.put("password", getPassword());
		return encodedServerCredentials;
	}

	public String getIPAddress() {
		return ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	private void setIPAddress(String ipAddress) {
		Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ipAddress);
		if (matcher.matches()) {
			this.ipAddress = ipAddress;
		} else {
			throw new IllegalArgumentException("Invalid IP Address");
		}
	}

	private void setUsername(String username) {
		if (username.compareTo("") != 0) {
			this.username = username;
		} else {
			throw new IllegalArgumentException(
					"username cannot be an empty string");
		}
	}

	private void setPassword(String password) {
		this.password = password;
	}

	private String ipAddress;
	private String username;
	private String password;
	private static final String IP_ADDRESS_PATTERN =
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
}
