/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lp20.aikuma.model.FileModel.FileType;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

import android.util.Log;

/**
 * The admin-file model (Incompleted)
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AdminFileModel {
	private static final String TAG = AdminFileModel.class.getCanonicalName();

	private static final String CLOUD_ID_FORMAT = "^v\\d{2}\\/.+\\/+\\/.+\\/.+\\/.+$";
	
	private static JSONObject prShareProgress;
	private static JSONObject prShareState;
	private static final String PRSHARE_PROGRESS_FILENAME = "prshare.json";
	
	/** Folder name where admin files are stored */
	public static final String ADMIN = "admin";
	/** Folder name where private-share tag files are stored */
	public static final String PRSHARE_PATH = "prshare/";
	
	/** Types of admin files */
	public enum AdminFileType { PRSHARE };
	
	/**
	 * Constructor of AdminFileModel(AdminFiles represent user actions)
	 * (Private-Share / Deletion)
	 * 
	 * @param versionName	versionName of the file-format
	 * @param userId		UserID who created the file
	 * @param id			Id of the file (File's name)
	 * @param type			Type of the file(speaker, mapping, ...)
	 */
	public AdminFileModel(
			String versionName, String userId, String id, AdminFileType type) {
		if(versionName == null || userId == null || id == null || type == null)
			throw new IllegalArgumentException("One of the parameter is null");
		
		this.versionName = versionName;
		this.userId = userId;
		this.id = id;
		this.fileType = type;
	}
	
	/**
	 * Factory function creating an instance of AdminFileModel from cloud-ID
	 * 
	 * @param cloudIdentifier	Cloud-identifier of the file
	 * @return	an instance of AdminFileModel
	 */
	public static AdminFileModel fromCloudId(String cloudIdentifier) {
		// (version)/admin/(login-user-ID)/(PRSHARE|DELETE)/(itemID)/(FileName)
		// (version)/admin/(PRSHARE|DELETE)/(userID)/(fileName)
		String[] splitCloudId = cloudIdentifier.split("\\/");
		if(Integer.valueOf(splitCloudId[0].substring(1)) < 3)
			return null;
		
		switch(AdminFileType.valueOf(splitCloudId[3])) {
		case PRSHARE:
			return new AdminFileModel(splitCloudId[0], splitCloudId[2], splitCloudId[5], AdminFileType.PRSHARE);
		default:
			return null;
		}
	}
	
	/**
	 * Tag the privately shared file (Create a tag file in admin path)
	 * 
	 * @param userId		UserID who shared the file
	 * @param targetUserId	UserID who are shared with
	 * @param fileId		Id of the file (File's name)
	 * @throws IOException	IOException when the file is created
	 */
	public static void logFileAsPrShared(
			String userId, String targetUserId, String fileId) throws IOException {
		if(prShareProgress == null) {
			loadPrShareFiles();	// throws IOException
		}
		if(((JSONArray)prShareState.get(targetUserId)).contains(fileId))
			return;
		
		JSONArray bufArr = (JSONArray) prShareProgress.get(targetUserId);
		if(bufArr.contains(fileId)) {
			return;
		} else {
			bufArr.add(userId + "-" + fileId);
			commitPrShareProgress();
		}
	}
	
	/**
	 * Log the change of the file's state
	 * 	- Private-share:	Approved -> Shared
	 * 
	 * @param userId		UserID who shared the file
	 * @param targetUserId	UserID who are shared with
	 * @param fileId		Id of the file (File's name)
	 * @throws IOException	IOException when the file is read/created
	 */
	public static void changeState(
			String userId, String targetUserId, String fileId) throws IOException {
		if(prShareProgress == null) {
			loadPrShareFiles();	// throws IOException
		}
		if(((JSONArray)prShareState.get(targetUserId)).contains(fileId))
			return;
		
		JSONArray bufArr = (JSONArray) prShareProgress.get(targetUserId);
		if(bufArr.contains(fileId)) {
			String tagFileName = fileId + "-" + targetUserId;
			File prShareTagFile = new File(getPrSharePath(AikumaSettings.getLatestVersion()), 
					userId + "/" + tagFileName);
			
			prShareTagFile.getParentFile().mkdirs();
			prShareTagFile.createNewFile();
			
			bufArr.remove(fileId);
			commitPrShareProgress(); // throws IOException
			((JSONArray)prShareState.get(targetUserId)).add(tagFileName);
		} else {
			return;
		}
		
	}
	
	
	/**
	 * Load the private-share states(approved/shared) of all users
	 * 
	 * @throws IOException	IOException when the files are read
	 */
	public static void loadPrShareFiles() throws IOException {
		loadPrShareProgress();
		loadPrShareState();
	}

	/**
	 * Load the private-share approval states and store it in prShareProgress 
	 */
	private static void loadPrShareProgress() throws IOException {
		File dir = FileIO.getOwnerPath(AikumaSettings.getLatestVersion(), ADMIN);
		File progFile = new File(dir, PRSHARE_PROGRESS_FILENAME);
		prShareProgress =  FileIO.readJSONObject(progFile);
	}
	
	
	private static void commitPrShareProgress() throws IOException {
		File dir = FileIO.getOwnerPath(AikumaSettings.getLatestVersion(), ADMIN);
		FileIO.writeJSONObject(new File(dir, PRSHARE_PROGRESS_FILENAME), prShareProgress);
	}
	
	private static List<List<String>> getFileListToPrShare() {
		List<List<String>> result = new ArrayList<List<String>>();
		for(String targetId : (Set<String>) prShareProgress.keySet()) {
			for(String userFileId : (List<String>) prShareProgress.get(targetId)) {
				//result.add(new List<String>)
			}
		}
		return result;
	}
	
	/**
	 * Load the private-share finish states and store it in prShareState
	 */
	private static void loadPrShareState() {
		File dir = getPrSharePath(AikumaSettings.getLatestVersion());
		File[] ownerIdDirs = dir.listFiles();
		for(File f : ownerIdDirs) {
			File[] prShareTagFiles = f.listFiles();
			for(File f2 : prShareTagFiles) {
				String fileName = f2.getName();
				int index = fileName.lastIndexOf("-");
				String targetUserId = fileName.substring(index+1);
				String fileId = fileName.substring(0, index);
				
				if(prShareState.containsKey(targetUserId)) {
					((JSONArray)prShareState.get(targetUserId)).add(fileId);
				} else {
					JSONArray bufArr = new JSONArray();
					bufArr.add(fileId);
					prShareState.put(targetUserId, bufArr);
				}
			}
		}
	}
	
	/**
	 * Get the recording owner's directory
	 * 
	 * @param versionName	versionName of the private-share tag files to read
	 * @return	A file representing the path of the recording owner's dir
	 */
	public static File getPrSharePath(String versionName) {
		File path = new File(
				FileIO.getOwnerPath(versionName, ADMIN), PRSHARE_PATH);
		path.mkdirs();
		return path;
	}
	
	/** Two states of the private-share tag files */
	public enum PrShareState {APPROVED, FINISHED};
	
	private String versionName;
	
	private String userId;
	
	private String id;
	
	private AdminFileType fileType;
	
}
