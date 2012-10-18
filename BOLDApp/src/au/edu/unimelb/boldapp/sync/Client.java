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
		try {
			// This code cannot be run before logging in, although no
			// documentation ever says that.
			result = apacheClient.setFileType(FTP.BINARY_FILE_TYPE);
			if (!result) {
				Log.i("sync", "setFileType returned false");
				return false;
			}
		} catch (IOException e) {
			//Log.i("sync", "thirdIOException");
			return false;
		}
		// Change to appropriate working directory
		if (loggedIn) {
			try {
				apacheClient.makeDirectory(serverBaseDir);
				result = cdServerBaseDir();
				//result = true;
				//Log.i("sync", "cdServerBaseDir result: " + result);
			} catch (IOException e ) {
				Log.i("sync", "fourthIOException");
				return false;
			}
		}
		//Log.i("sync", "final");
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
		Log.i("zxcv", "pushResult: " + pushResult);
		Log.i("zxcv", "pullResult: " + pullResult);
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
			Boolean result = null;
			for (String filename : clientFilenames) {
				file = new File(clientDir.getPath() + "/" + filename);
				if (!file.getName().endsWith(".inprogress")) {
					if (!file.isDirectory()) {
						if (!serverFilenames.contains(filename)) {
							result = pushFile(directoryPath, file);
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
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Push an individual file to the server.
	 *
	 * @param	file	The file to be pushed.
	 * @return	true if successful; false otherwise.
	 */
	public boolean pushFile(String directoryPath, File file) {
		boolean result = false;
		try {
			InputStream stream = new FileInputStream(file);
			result = apacheClient.storeFile(
					serverBaseDir + directoryPath + "/" + file.getName() +
							".inprogress",
					stream);
			stream.close();
			apacheClient.rename(
					serverBaseDir + directoryPath + "/" + file.getName() +
							".inprogress",
					serverBaseDir + directoryPath + "/" + file.getName());
		} catch (IOException e) {
			return false;
		}
		return result;
	}

	/**
	 * Pull an individual file from the server.
	 *
	 * @param	file	The file to be pulled.
	 * @return	true if successful; false otherwise.
	 */
	public boolean pullFile(String directoryPath, File file) {
		Log.i("zxcv", "pullFile: " + directoryPath + " " + file.getName());
		boolean result = false;
		try {
			File inProgressFile = new File(file.getPath() + ".inprogress");
					/*clientBaseDir + directoryPath + "/" + file.getName() +
							".inprogress");*/
			Log.i("zxcv", "inprogressfilename  " + inProgressFile.getPath());
			OutputStream stream = new FileOutputStream(inProgressFile);
			result = apacheClient.retrieveFile(
					serverBaseDir + directoryPath + "/" +
							file.getName(),
					stream);
			stream.close();
			Log.i("zxcv", "blah: " + inProgressFile.getName() + " " + result);
			inProgressFile.renameTo(file);
		} catch (IOException e) {
			Log.e("zxcv", "borg" , e);
			return false;
		}
		return result;
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
				if (!file.getName().endsWith(".inprogress")) {
					if (serverFile.isDirectory()) {
						file.mkdirs();
						result = pullDirectory(
								directoryPath + "/" + serverFile.getName());
						if (!result) {
							return false;
						}
					} else {
						if (!clientFilenames.contains(serverFile.getName())) {
							result = pullFile(directoryPath, file);
							if (!result) {
								return false;
							}
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
	 * Finds the first writable directory on the server.
	 *
	 * @return	the first writable directory on the server.
	 */
/*
	public String findServerBaseDir() {
		return findWritableDir("/");
	}
	private String findWritableDir(String startPath) {
		try {
			apacheClient.changeWorkingDirectory(startPath);
			List<FTPFile> directories;
			String writable;
			if (apacheClient.makeDirectory("bold") == false) {
				directories = Arrays.asList(apacheClient.listDirectories());
				for (FTPFile dir : directories) {
					System.out.println(dir.getName());
					System.out.println(startPath + dir.getName());
					writable = findWritableDir(startPath + dir.getName());
					if (writable != null) {
						return writable;
					}
				}
			} else {
				return apacheClient.printWorkingDirectory();
			}
		} catch (IOException e) {
			return null;
		}
		return null;
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
	private String clientBaseDir;

	/**
	 * The path to the working directory.
	 */
	private String serverBaseDir;

}
