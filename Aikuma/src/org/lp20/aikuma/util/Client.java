/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

	private static final String BOLD_DIR = "aikuma";

	/**
	 * Set up the working directory for the client.
	 *
	 * @param	clientBaseDir	The working directory for the client.
	 */
	public void setClientBaseDir(String clientBaseDir) {
		this.clientBaseDir = clientBaseDir;
	}

	/**
	 * Set up the working directory for the server.
	 *
	 * @param	serverBaseDir	The working directory for the server.
	 */
	private void setServerBaseDir(String serverBaseDir) {
		this.serverBaseDir = serverBaseDir;
	}

	/**
	 * Get the working directory for the server.
	 *
	 * @return	A string representing the working directory.
	 */
	public String getServerBaseDir() {
		return this.serverBaseDir;
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
				Log.i("sync", "Connected to: " + serverURI);
			} catch (SocketException e) {
				Log.e("sync", "apacheClient.connect() threw a SocketException", e);
				return false;
			} catch (IOException e) {
				Log.e("sync", "apacheClient.connect() threw an IOException", e);
				return false;
			}
		}
		if (!loggedIn) {
			try {
				result = apacheClient.login(username, password);
				if (!result) {
					Log.i("sync", "apacheClient.login returned false: " +
						username + " " + password);
				}
				loggedIn = result;
			} catch (IOException e) {
				Log.e("sync", "apacheClient.login() threw an IOException", e);
				return false;
			}
		}
		try {
			// This code cannot be run before logging in, although no
			// documentation ever says that.
			result = apacheClient.setFileType(FTP.BINARY_FILE_TYPE);
			if (!result) {
				Log.i("sync", "apacheClient.setFileType returned false");
				return false;
			}
		} catch (IOException e) {
			Log.e("sync", "apacheClient.setFileType threw an IOException", e);
			return false;
		}
		// Change to appropriate working directory
		if (loggedIn) {
			//try {
				String serverBaseDir = findServerBaseDir();
				if (serverBaseDir == null) {
					logout();
					Log.i("sync", "serverbaseDir is null");
					return false;
				} else {
					setServerBaseDir(findServerBaseDir());
					result = cdServerBaseDir();
					if (!result) {
						Log.i("sync", "cdServerBaseDir returned false");
					}
				}
				//result = true;
			//} catch (IOException e ) {
			//	return false;
			//}
		}
		return result;
	}


	/**
	 * Logout of a server.
	 *
	 * @return	true if successful; false otherwise.
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
	 * @return	true if successful; false otherwise.
	 */
	public boolean pushDirectory(String directoryPath) {

		Log.i("sync", "Pushing directory: " + directoryPath);
		// Get the specified client side directory.
		Log.i("sync", "clientBaseDir: " + clientBaseDir);
		File clientDir;
		if (clientBaseDir.endsWith("/")) {
			Log.i("sync", "endswith /");
			clientDir = new File(clientBaseDir + directoryPath);
		} else {
			Log.i("sync", "doesn't end with /");
			clientDir = new File(clientBaseDir + "/" + directoryPath);
		}
		if (!clientDir.isDirectory()) {
			return false;
		}

		// Attempt to make the directory on the server side and then change
		// into it.
		try {
			apacheClient.makeDirectory(
					serverBaseDir + "/" + directoryPath);
			apacheClient.changeWorkingDirectory(
					serverBaseDir + "/" + directoryPath);
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
								serverBaseDir + "/" + directoryPath);
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
	 * @param	directoryPath	Server side directory to push the file to.
	 * @param	file	The file to be pushed.
	 * @return	true if successful; false otherwise.
	 */
	public boolean pushFile(String directoryPath, File file) {
		boolean result = false;
		try {
			InputStream stream = new FileInputStream(file);
			result = apacheClient.storeFile(
					serverBaseDir + "/" + directoryPath + "/" + file.getName() +
							".inprogress",
					stream);
			stream.close();
			apacheClient.rename(
					serverBaseDir + "/" + directoryPath + "/" + file.getName() +
							".inprogress",
					serverBaseDir + "/" + directoryPath + "/" + file.getName());
		} catch (IOException e) {
			return false;
		}
		if (result) {
		} else {
		}
		return result;
	}

	/**
	 * Pull an individual file from the server.
	 *
	 * @param	file	The file to be pulled.
	 * @param	directoryPath	The directory the file resides in, relative to
	 * the base directory.
	 * @return	true if successful; false otherwise.
	 */
	public boolean pullFile(String directoryPath, File file) {
		boolean result = false;
		try {
			File inProgressFile = new File(file.getPath() + ".inprogress");
					/*clientBaseDir + directoryPath + "/" + file.getName() +
							".inprogress");*/
			OutputStream stream = new FileOutputStream(inProgressFile);
			result = apacheClient.retrieveFile(
					serverBaseDir + "/" + directoryPath + "/" +
							file.getName(),
					stream);
			stream.close();
			inProgressFile.renameTo(file);
		} catch (IOException e) {
			return false;
		}
		return result;
	}

	/**
	 * Recursively pull the directory from the server.
	 *
	 * @param	directoryPath	the relative path from the base directory
	 * @return	true if successful; false otherwise.
	 */
	public boolean pullDirectory(String directoryPath) {

	Log.i("sync", "Pulling directory: " + directoryPath);

		// Attempt to change to the directory on the server side.
		try {
			boolean result;
			result = apacheClient.changeWorkingDirectory(
					serverBaseDir + "/" + directoryPath);
			if (!result) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}

		File clientDir;
		if (clientBaseDir.endsWith("/")) {
			clientDir = new File(clientBaseDir + directoryPath);
		} else {
			clientDir = new File(clientBaseDir + "/" + directoryPath);
		}
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
			boolean result = apacheClient.changeWorkingDirectory(serverBaseDir);
			return result;
		} catch (IOException e) {
			return false;
		}
	}

	//public boolean deleteFileAbsolute(String filePath) {
	//	String originalWorkingDir = apacheClient.printWorkingDirectory();
	//	String splitPath = filePath.split("/");
	//	String parentPath = 
	//}

	/**
	 * Recursively deletes a directory on the server
	 *
	 * @param	dirPath	The path to the directory (absolute or relative)
	 * @return	true if successful; false otherwise.
	 **/
	public boolean deleteServerDir(String dirPath) {
		String[] dirPathSplit = dirPath.split("/");
		String dirName = dirPathSplit[dirPathSplit.length - 1];
		try {
			try {
				if (dirPath.startsWith("/")) {
					if(!apacheClient.changeWorkingDirectory(dirPath)) {
						return false;
					};
				} else {
					if (!apacheClient.changeWorkingDirectory(
							serverBaseDir + "/" +  dirPath)) {
						return false;
					}
				}
			} catch (IOException e) {
				return false;
			}
			List<FTPFile> serverFiles = Arrays.asList(
					apacheClient.listFiles());
			if (serverFiles.size() == 0) {
				apacheClient.changeToParentDirectory();
				return apacheClient.removeDirectory(dirName);
			} else {
				for (FTPFile file : serverFiles) {
					if (!file.isDirectory()) {
						apacheClient.deleteFile(file.getName());
					} else {
						deleteServerDir(apacheClient.printWorkingDirectory() +
						"/" + file.getName());
					}
				}
				apacheClient.changeToParentDirectory();
				return apacheClient.removeDirectory(dirName);
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Finds the first writable directory on the server.
	 *
	 * @return	the first writable directory on the server.
	 */

	public String findServerBaseDir() {
		String dir = findWritableDir("/");
		return dir;
	}

	/**
	 * Find the first writable directory under the specified path. Note that it
	 * uses a depth-first approach to determine what is "first writable".
	 *
	 * @param	startPath	The directory to start from
	 * @return	A string representation of the writable directory.
	 */
	private String findWritableDir(String startPath) {
		try {
			if (!apacheClient.changeWorkingDirectory(startPath)) {
				return null;
			}

			// Try to create a directory. If we succeed, we know that this
			// directory is writable.
			String unlikelyDir = UUID.randomUUID().toString();
			if (apacheClient.makeDirectory(unlikelyDir)) {
				apacheClient.removeDirectory(unlikelyDir);
				return apacheClient.printWorkingDirectory();
			} else {

				List<FTPFile> directories = 
						Arrays.asList(apacheClient.listDirectories());
				String writablePath;
				for (FTPFile dir : directories) {

					// Act robustly in the face of different directory notation
					if (startPath.endsWith("/")) {
						writablePath = findWritableDir(startPath + dir.getName());
					} else {
						writablePath = findWritableDir(startPath + "/" + dir.getName());
					}

					if (writablePath != null) {
						return writablePath;
					}
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
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
