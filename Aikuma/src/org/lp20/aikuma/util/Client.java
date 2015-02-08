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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		uploadFileCount = 0;
		downloadFileCount = 0;
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
					SyncUtil.updateSyncTextView(
							"There is no writable 'aikuma' directory on the server");
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
		uploadFileCntTotal = countUploadFiles();
		downloadFileCntTotal = countDownloadFiles();
		boolean pushResult = (push() != -1);
		boolean pullResult = (pull() != -1);
		return pushResult && pullResult;
	}

	/**
	 * Push files to server that are on the client but not the server.
	 *
	 * @return	0 if successful; -1 otherwise.
	 */
	public int push() {
		return pushDirectory(".", 0);
	}
	
	/**
	 * Count files to be uploaded that are on the client
	 * 
	 * @return	>=0 if successful; -1 otherwise
	 */
	public int countUploadFiles() {
		return pushDirectory(".", 1);
	}

	/**
	 * Pull file from the server that are on the server but not on the client.
	 *
	 * @return	0 if successful; -1 otherwise.
	 */
	public int pull() {
		return pullDirectory(".", 0);
	}
	
	/**
	 * Count files to be downloaded that are on the server
	 * 
	 * @return	>=0 if successful; -1 otherwise
	 */
	public int countDownloadFiles() {
		return pullDirectory(".", 1);
	}

	/**
	 * Recursively push the directory to server.(mode: 0)
	 * Recursively count the files to be uploaded.(mode: 1)
	 * (local-variable 'result' is always -1 for an exception and can only have
	 *  two values(-1, 0) when mode is 0)
	 * (Call SyncUtil.updateSyncTextView() )
	 *
	 * @param	directoryPath	the relative path from the base directory
	 * @param	mode			0: push files to directory / 1: count files to be uploaded
	 * @return	>=0 if successful; -1 otherwise.
	 */
	public int pushDirectory(String directoryPath, int mode) {
		if(mode == 0) {
			Log.i("sync", "Pushing directory: " + directoryPath);
		} else {
			Log.i("sync", "Counting files to be uploaded in " + 
					clientBaseDir + "/" + directoryPath);
		}
		
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
			return -1;
		}
		
		// Attempt to make the directory on the server side and then change
		// into it.
		try {
			apacheClient.makeDirectory(
					serverBaseDir + "/" + directoryPath);
			apacheClient.changeWorkingDirectory(
					serverBaseDir + "/" + directoryPath);
		} catch (IOException e) {
			return -1;
		}

		int result = 0;
		try {
			List<String> clientFilenames = Arrays.asList(clientDir.list());
			List<String> serverFilenames = Arrays.asList(
					apacheClient.listNames());
			File file = null;
			
		
			for (String filename : clientFilenames) {
				Log.i("sync", "name: " + filename);
				file = new File(clientDir.getPath() + "/" + filename);
				if (!filename.endsWith(".inprogress") && !filename.startsWith("default_languages") && !filename.startsWith("index")) {
					// If file doesn't end with '.inprogress', 
					// If it is not the one Aikuma previously made(index, language-setting file). Then check the file.
					if (!file.isDirectory()) { // If it is not a directory

						if (!serverFilenames.contains(filename)) { 
							// and if Server doesn't have the file, upload/count.
							if(mode == 0) {
								if(pushFile(directoryPath, file)) {
									showCurrentCount("Upload", file.getName());
								} else {
									return -1;
								}
							} else {	//Count-mode
								result++;
							}
							
						}
					} else { // If it is a directory, search the folder(DFS by pushDirecctory)
						apacheClient.makeDirectory(filename);
						if(mode == 0) {
							result = pushDirectory(directoryPath + "/" + filename, 0);
							if(result == -1)
								return -1;
						} else {	//Count-mode
							result += pushDirectory(directoryPath + "/" + filename, 1);
						}
						
						apacheClient.changeWorkingDirectory(
								serverBaseDir + "/" + directoryPath);
					}
				}
			}
		} catch (IOException e) {
			Log.e("sync", e.getMessage());
			return -1;
		}

		return result;
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
	 * @param	outputFile		File to be output in the client device (should be in the same directory with file)
	 * @return	true if successful; false otherwise.
	 */
	public boolean pullFile(String directoryPath, File file, File outputFile) {
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
			inProgressFile.renameTo(outputFile);
		} catch (IOException e) {
			return false;
		}
		return result;
	}
	
	private boolean pullFile(String directoryPath, File file) {
		return pullFile(directoryPath, file, file);
	}
	
	/**
	 * Recursively pull the directory from the server.
	 * Recursively count the files to be downloaded.(mode: 1)
	 * (local-variable 'result' is always -1 for an exception and can only have
	 *  two values(-1, 0) when mode is 0)
	 * (Call SyncUtil.updateSyncTextView() )
	 *
	 * @param	directoryPath	the relative path from the base directory
	 * @param	mode			0: pull files to directory / 1: count files to be downloaded
	 * @return	>=0 if successful; -1 otherwise.
	 */
	public int pullDirectory(String directoryPath, int mode) {
		if(mode == 0) {
			Log.i("sync", "Pulling directory: " + directoryPath);
		} else {
			Log.i("sync", "Counting files to be downloaded in " + 
					serverBaseDir + "/" + directoryPath);
		}
	
		// Attempt to change to the directory on the server side.
		try {
			boolean result;
			result = apacheClient.changeWorkingDirectory(
					serverBaseDir + "/" + directoryPath);
			if (!result) {
				return -1;
			}
		} catch (IOException e) {
			return -1;
		}

		File clientDir;
		if (clientBaseDir.endsWith("/")) {
			clientDir = new File(clientBaseDir + directoryPath);
		} else {
			clientDir = new File(clientBaseDir + "/" + directoryPath);
		}
		clientDir.mkdirs();

		int result = 0;
		try {
			List<String> clientFilenames = Arrays.asList(clientDir.list());
			List<FTPFile> serverFiles = Arrays.asList(
					apacheClient.listFiles());
			File file = null;
			OutputStream stream = null;
			
			for (FTPFile serverFile : serverFiles) {
				file = new File(clientDir, serverFile.getName());
				if (!file.getName().endsWith(".inprogress")) {
					if (serverFile.isDirectory()) {
						file.mkdirs();
						if(mode == 0) {
							result = pullDirectory(
									directoryPath + "/" + serverFile.getName(), 0);
							if(result == -1)
								return -1;
						} else {	//Count-mode
							result += pullDirectory(
									directoryPath + "/" + serverFile.getName(), 1);
						}
					} else {
						if (!clientFilenames.contains(serverFile.getName())) {
							if(mode == 0) {
								if(pullFile(directoryPath, file)) {
									showCurrentCount("Download", file.getName());
								} else {
									return -1;
								}
							} else {	//Count-mode
								result++;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		return result;
	}

	/**
	 * Show the sync-feedback to the user
	 * 
	 * @param streamDirection	"Download" or "Upload"
	 */
	private void showCurrentCount(String streamDirection, String fileName) {
		String status;
		if(streamDirection.equals("Upload")) {
			uploadFileCount++;
			status = (streamDirection + ": " + 
					uploadFileCount + "/" + uploadFileCntTotal + 
					"\n(" + fileName + ")");
		} else {
			downloadFileCount++;
			status = (streamDirection + ": " + 
					downloadFileCount + "/" + downloadFileCntTotal + 
					"\n(" + fileName + ")");
		}
		SyncUtil.updateSyncTextView(status);
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
	 * Find the writable 'aikuma' directory at the root directory on the server.
	 *
	 * @return	the first writable directory on the server.
	 */
	public String findServerBaseDir() {
		String dir = findAikumaDir("/");
		return dir;
	}

	/**
	 * Find the writable 'aikuma' folder, if not exist try to create it
	 * 
	 * @param 	startPath		root directory
	 * @return	A string representation of the aikuma directory.
	 */
	private String findAikumaDir(String startPath) {
		try {
			if (!apacheClient.changeWorkingDirectory(startPath)) {
				return null;
			}
			
			//Try creating the 'aikuma' directory
			if(apacheClient.makeDirectory(startPath + "aikuma")) {
				return (startPath + "aikuma");
			}
			
			// If there is aikuma folder already made, 
			// Find aikuma folder from the root directory
			List<FTPFile> directories = 
					Arrays.asList(apacheClient.listDirectories());
			for(FTPFile dir : directories) {
				Log.i("sync", "Dir: " + startPath + dir.getName());
				if(dir.getName().equals("aikuma")) {
					// Check if '/aikuma' directory is writable
					apacheClient.changeWorkingDirectory(startPath + dir.getName());
					String unlikelyDir = UUID.randomUUID().toString();
					if (apacheClient.makeDirectory(unlikelyDir)) {
						apacheClient.removeDirectory(unlikelyDir);
						return (startPath + dir.getName());
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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
			// ('/' and '/(folder-name)' which FTP server shares are checked)
			// ('/': If FTP server configuration doesn't share '/', '/' is not writable)
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
	
	/**
	 * Number of files uploaded
	 */
	private int uploadFileCount;
	private int uploadFileCntTotal;
	
	/**
	 * Number of files downloaded
	 */
	private int downloadFileCount;
	private int downloadFileCntTotal;
}
