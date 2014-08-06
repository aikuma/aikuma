/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.lp20.aikuma.util.FileIO;

/**
 * Contains information about the supplied server credentials for connecting to
 * an FTP server.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ServerCredentials {

	/**
	 * Constructor
	 *
	 * @param	ipAddress	The IP address of the server.
	 * @param	username	The username of a user on the server who will be
	 * logged in as.
	 * @param	password	The password corresponding to the user.
	 * @param	syncActivated	A flag indicating whether automatic sync is
	 * activated or not
	 * @param	lastSyncDate	Latest sync success date
	 */
	public ServerCredentials(String ipAddress, String username, 
			String password, boolean syncActivated, String lastSyncDate) {
		setIPAddress(ipAddress);
		setUsername(username);
		setPassword(password);
		setSyncActivated(syncActivated);
		setLastSyncDate(lastSyncDate);
	}

	/**
	 * Write the server credentials to file.
	 *
	 * @throws	IOException	If the credentials cannot be written.
	 */
	public void write() throws IOException {
		JSONObject encodedServerCredentials = this.encode();
		FileIO.writeJSONObject(
				new File(FileIO.getAppRootPath(), "server_credentials.json"),
				encodedServerCredentials);
	}

	/**
	 * Reads server credentials from file.
	 *
	 * @return	A ServerCredentials object representing the stored server
	 * credentials
	 * @throws	IOException	If the credentials cannot be read.
	 */
	public static ServerCredentials read() throws IOException {
		JSONObject encodedServerCredentials = FileIO.readJSONObject(
				new File(FileIO.getAppRootPath(), "server_credentials.json"));
		String ipAddress = (String) encodedServerCredentials.get("ipAddress");
		String username = (String) encodedServerCredentials.get("username");
		String password = (String) encodedServerCredentials.get("password");
		Boolean syncActivated = (Boolean)
				encodedServerCredentials.get("syncActivated");
		String lastSyncDate = (String) encodedServerCredentials.get("lastSyncDate");
		return new ServerCredentials(
				ipAddress, username, password, syncActivated, lastSyncDate);
	}

	// Encodes the server credentials as a JSON object.
	private JSONObject encode() {
		JSONObject encodedServerCredentials = new JSONObject();
		encodedServerCredentials.put("ipAddress", getIPAddress());
		encodedServerCredentials.put("username", getUsername());
		encodedServerCredentials.put("password", getPassword());
		encodedServerCredentials.put("syncActivated", getSyncActivated());
		encodedServerCredentials.put("lastSyncDate", getLastSyncDate());
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

	public String getLastSyncDate() {
		return lastSyncDate;
	}
	
	//Sets the IP address but will only accept valid IPv4 addresses.
	private void setIPAddress(String ipAddress) {
		Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ipAddress);
		if (matcher.matches()) {
			this.ipAddress = ipAddress;
		} else {
			throw new IllegalArgumentException("Invalid IP Address");
		}
	}

	// Sets the username, but will not accept empty strings.
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

	private void setSyncActivated(boolean syncActivated) {
		this.syncActivated = syncActivated;
	}

	public boolean getSyncActivated() {
		return this.syncActivated;
	}

	/**
	 * Sets the latest sync-date.
	 * (Called by SyncUtil)
	 * 
	 * @param lastSyncDate	Lastest success sync date
	 */
	public void setLastSyncDate(String lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}
	
	private String ipAddress;
	private String username;
	private String password;
	private boolean syncActivated;
	private String lastSyncDate;
	// Used to ensure the IP address is valid syntactically
	private static final String IP_ADDRESS_PATTERN =
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
}
