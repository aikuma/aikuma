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

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
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
	public void setClientBaseDir(String clientBaseDir) {
		this.clientBaseDir = clientBaseDir;
	}

	/**
	 * Set up the working directory for the server.
	 */
	public void setServerBaseDir(String serverBaseDir) {
		this.serverBaseDir = serverBaseDir;
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
				Log.i("sync", "socketexception");
				return false;
			} catch (IOException e) {
				Log.i("sync", "firstIOException");
				return false;
			}
		}
		if (!loggedIn) {
			try {
				result = apacheClient.login(username, password);
				loggedIn = result;
			} catch (IOException e) {
				Log.i("sync", "secondIOException");
				return false;
			}
		}
		// Change to appropriate working directory
		if (loggedIn) {
			try {
				apacheClient.makeDirectory(serverBaseDir);
				result = cdServerBaseDir();
			} catch (IOException e ) {
				Log.i("sync", "thirdIOException");
				return false;
			}
		}
		Log.i("sync", "final");
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
		return pushDirectory(".");
	}

	/**
	 * Pull file from the server that are on the server but not on the client.
	 *
	 * @return	true if successful; false otherwise.
	 */
	public boolean pull() {
		return pullDirectory(".");
	}

	/**
	 * Recursively push the directory to server.
	 *
	 * @param	directoryPath	the relative path from the base directory
	 */
	public boolean pushDirectory(String directoryPath) {
		// Get the specified client side directory.
		File clientDir = new File(clientBaseDir + directoryPath);
		if (!clientDir.isDirectory()) {
			return false;
		}

		// Attempt to make the directory on the server side and then change
		// into it.
		try {
			apacheClient.makeDirectory(
					serverBaseDir + directoryPath);
			apacheClient.changeWorkingDirectory(
					serverBaseDir + directoryPath);
		} catch (IOException e) {
			return false;
		}

		try {
			List<String> clientFilenames = Arrays.asList(clientDir.list());
			List<String> serverFilenames = Arrays.asList(
					apacheClient.listNames());
			File file = null;
			InputStream stream = null;
			Boolean result = null;
			for (String filename : clientFilenames) {
				file = new File(clientDir + "/" + filename);
				if (!file.isDirectory()) {
					if (!serverFilenames.contains(filename)) {
						stream = new FileInputStream(file);
						result = apacheClient.storeFile(
								serverBaseDir + directoryPath + "/" + filename,
								stream);
						stream.close();
						if (!result) {
							return false;
						}
					}
				} else {
					apacheClient.makeDirectory(filename);
					result = pushDirectory(directoryPath + "/" + filename);
					apacheClient.changeWorkingDirectory(
							serverBaseDir + directoryPath);
					if (!result) {
						return false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Recursively pull the directory from the server.
	 *
	 * @param	directoryPath	the relative path from the base directory
	 */
	public boolean pullDirectory(String directoryPath) {

		// Attempt to change to the directory on the server side.
		try {
			boolean result;
			result = apacheClient.changeWorkingDirectory(
					serverBaseDir + directoryPath);
			if (!result) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}

		File clientDir = new File(clientBaseDir + directoryPath);
		clientDir.mkdirs();

		try {
			List<String> clientFilenames = Arrays.asList(clientDir.list());
			List<FTPFile> serverFiles = Arrays.asList(
					apacheClient.listFiles());
			File file = null;
			OutputStream stream = null;
			Boolean result = null;
			for (FTPFile serverFile : serverFiles) {
				file = new File(
						clientBaseDir + directoryPath + "/" +
						serverFile.getName());
				if (serverFile.isDirectory()) {
					file.mkdirs();
					result = pullDirectory(
							directoryPath + "/" + serverFile.getName());
					if (!result) {
						return false;
					}
				} else {
					if (!clientFilenames.contains(serverFile.getName())) {
						stream = new FileOutputStream(file);
						result = apacheClient.retrieveFile(
								serverBaseDir + directoryPath + "/" +
								serverFile.getName(),
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


	/**
	 * Change the working directory of the apache client.
	 *
	 * @return	true if successful; false otherwise.
	 */
	public boolean cdServerBaseDir() {
		try {
			return apacheClient.changeWorkingDirectory(serverBaseDir);
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
	private String clientBaseDir;

	/**
	 * The path to the working directory.
	 */
	private String serverBaseDir;

}
