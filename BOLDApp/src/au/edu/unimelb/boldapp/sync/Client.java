package au.edu.unimelb.boldapp.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

/**
 * FTP client that allows the application to sync it's data with a server.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Client {

	/**
	 * Set up the working directory for the client.
	 */
	public void setClientWorkingDir(String clientWorkingDir) {
		this.clientWorkingDir = clientWorkingDir;
	}

	/**
	 * Set up the working directory for the server.
	 */
	public void setServerWorkingDir(String serverWorkingDir) {
		this.serverWorkingDir = serverWorkingDir;
	}

	/**
	 * Login to a server.
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
		// Change to appropriate working directory
		if (loggedIn) {
			try {
				result = apacheClient.changeWorkingDirectory(serverWorkingDir);
			} catch (IOException e ) {
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

	/**
	 * Push files to server that are on the client but not the server.
	 */
	public void push() throws IOException {
		File clientDir = new File(clientWorkingDir);
		List<String> clientFilenames = Arrays.asList(clientDir.list());
		List<String> serverFilenames =
				Arrays.asList(apacheClient.listNames());
		System.out.println("" + clientFilenames);
		System.out.println("" + serverFilenames);
		File file = null;
		InputStream stream = null;
		Boolean result = null;
		for (String filename : clientFilenames) {
			if (!serverFilenames.contains(filename)) {
				System.out.println(filename + " not on the server.");
				file = new File(clientDir + "/" + filename);
				stream = new FileInputStream(file);
				result = apacheClient.storeFile(
						serverWorkingDir + "/" + filename, 
						stream);
				System.out.println("result " + result);
			}
		}

		clientFilenames = Arrays.asList(clientDir.list());
		serverFilenames =
				Arrays.asList(apacheClient.listNames());

		System.out.println("" + clientFilenames);
		System.out.println("" + serverFilenames);

	}

	/*
	public boolean serverDeleteAll() {
		boolean result = apacheClient.deleteFile(
	}
	*/

	/**
	 * The Apache FTPClient used by this FTPClient.
	 */
	private org.apache.commons.net.ftp.FTPClient apacheClient;

	/**
	 * Whether the client is logged in or not.
	 */
	private boolean loggedIn = false;

	/**
	 * The path to the client's working directory.
	 */
	private String clientWorkingDir;

	/**
	 * The path to the working directory.
	 */
	private String serverWorkingDir;

}
