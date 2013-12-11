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
	 */
	public void setClientBaseDir(String clientBaseDir) {
		this.clientBaseDir = clientBaseDir;
	}

	/**
	 * Set up the working directory for the server.
	 */
	private void setServerBaseDir(String serverBaseDir) {
		this.serverBaseDir = serverBaseDir;
	}

	/**
	 * Get the working directory for the server.
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
			} catch (SocketException e) {
				Log.i("syncClient", "error 1");
				return false;
			} catch (IOException e) {
				Log.i("syncClient", "error 2");
				return false;
			}
		}
		if (!loggedIn) {
			try {
				result = apacheClient.login(username, password);
				loggedIn = result;
			} catch (IOException e) {
				Log.i("syncClient", "error 3");
				return false;
			}
		}
		try {
			// This code cannot be run before logging in, although no
			// documentation ever says that.
			result = apacheClient.setFileType(FTP.BINARY_FILE_TYPE);
			if (!result) {
				Log.i("syncClient", "error 4");
				return false;
			}
		} catch (IOException e) {
			Log.i("syncClient", "error 5");
			return false;
		}
		// Change to appropriate working directory
		if (loggedIn) {
			//try {
				String serverBaseDir = findServerBaseDir();
				if (serverBaseDir == null) {
					logout();
					Log.i("syncClient", "error 6");
					return false;
				} else {
					setServerBaseDir(findServerBaseDir());
					result = cdServerBaseDir();
					if (result == false) {
						Log.i("syncClient", "error 7");
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
		File clientDir;
		if (clientBaseDir.endsWith("/")) {
			clientDir = new File(clientBaseDir + directoryPath);
		} else {
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
	 */
	public boolean pullDirectory(String directoryPath) {

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
	 */
	private String findWritableDir(String startPath) {
		try {
			if (!apacheClient.changeWorkingDirectory(startPath)) {
				Log.i("syncClient", "couldn't change working directory");
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
			Log.i("syncClient", "IOException");
			return null;
		}
		Log.i("syncClient", "final null");
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
