package au.edu.unimelb.boldapp.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	 * Standard constructor
	 */
	public Client() {
		apacheClient = new org.apache.commons.net.ftp.FTPClient();
		try {
			apacheClient.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			// Do nothing
		}
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
				apacheClient.makeDirectory(serverWorkingDir);
				result = cdServerWorkingDir();
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
	 * Sync the client with the server.
	 *
	 * @return	true if completely successful; false otherwise.
	 */
	public boolean sync() {
		boolean pushResult = push();
		boolean pullResult = pull();
		return pushResult && pullResult;
	}

	/**
	 * Push files to server that are on the client but not the server.
	 *
	 * @return	true if successful; false otherwise.
	 */
	public boolean push() {
		File clientDir = new File(clientWorkingDir);
		List<String> clientFilenames = Arrays.asList(clientDir.list());
		cdServerWorkingDir();
		try {
			List<String> serverFilenames =
					Arrays.asList(apacheClient.listNames());
			File file = null;
			InputStream stream = null;
			Boolean result = null;
			for (String filename : clientFilenames) {
				if (!serverFilenames.contains(filename)) {
					file = new File(clientDir + "/" + filename);
					if (!file.isDirectory()) {
						stream = new FileInputStream(file);
						result = apacheClient.storeFile(
								serverWorkingDir + "/" + filename, 
								stream);
						stream.close();
						if (!result) {
							return false;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

/*
	public boolean pushDirectory(String directoryName) {
		File file = null;
		InputStream stream = null;
		Boolean result = null;

		File dir = new File(directoryName);
		if (!dir.isDirectory()) {
			return false;
		}
		List<String> clientFilenames = Arrays.asList(dir.list());
		for (String filename : clientFilenames) {
			if (!serverFilenames.contains(filename)) {
				file = new File(clientDir + "/" + filename);
				if (!file.isDirectory()) {
					stream = new FileInputStream(file);
					result = apacheClient.storeFile(
							serverWorkingDir + "/" + filename,
							stream);
					stream.close();
					if (!result) {
						return false;
					}
				}
			}
		}
	}
*/

	/**
	 * Pull file from the server that are on the server but not on the client.
	 *
	 * @return	true if successful; false otherwise.
	 */
	public boolean pull() {
		File clientDir = new File(clientWorkingDir);
		List<String> clientFilenames = Arrays.asList(clientDir.list());
		cdServerWorkingDir();
		try {
			List<String> serverFilenames =
					Arrays.asList(apacheClient.listNames());
			File file = null;
			OutputStream stream = null;
			Boolean result = null;
			for (String filename : serverFilenames) {
				if (!clientFilenames.contains(filename)) {
					file = new File(clientDir + "/" + filename);
					stream = new FileOutputStream(file);
					result = apacheClient.retrieveFile(
							serverWorkingDir + "/" + filename,
							stream);
					stream.close();
					if (!result) {
						return false;
					}
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Change the working directory of the apache client.
	 *
	 * @return	true if successful; false otherwise.
	 */
	public boolean cdServerWorkingDir() {
		try {
			return apacheClient.changeWorkingDirectory(serverWorkingDir);
		} catch (IOException e) {
			return false;
		}
	}

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
