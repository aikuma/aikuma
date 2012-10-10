package au.edu.unimelb.boldapp.sync;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;

/**
 * FTP client that allows the application to sync it's data with a server.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Client {

	/**
	 * The Apache FTPClient used by this FTPClient
	 */
	private org.apache.commons.net.ftp.FTPClient apacheClient;

	/**
	 * Whether the client is logged in or not.
	 */
	private boolean loggedIn = false;

	/**
	 * Login to a server
	 *
	 * @param	serverURI	The server URI.
	 * @param	username	The username.
	 * @param	password	The password.
	 *
	 * @return	true if the login was successful; false otherwise.
	 */
	public boolean login(String serverURI, String username, String password) {
		if (apacheClient == null) {
			apacheClient = new org.apache.commons.net.ftp.FTPClient();
		}
		boolean result = false;
		if (!apacheClient.isConnected()) {
			try {
				apacheClient.connect(serverURI);
			} catch (SocketException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		if (!loggedIn) {
			try {
				result = apacheClient.login(username, password);
				loggedIn = result;
			} catch (IOException e) {
				return false;
			}
		}
		return result;
	}

	
	/**
	 * Logout of a server.
	 */
	public boolean logout() {
		if (apacheClient == null) {
			return false;
		}
		if (loggedIn) {
			try {
				boolean result;
				result = apacheClient.logout();
				loggedIn = !result;
				return result;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

}
