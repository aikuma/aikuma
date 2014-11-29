/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.util;

import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;

/**
 * The class storing setting parameters which can be accessed
 * by all Android components in the application
 * (Global Parameters)
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AikumaSettings {
	/**
	 * Setting value for backup
	 */
	public static boolean isBackupEnabled;
	/**
	 * Setting value for auto-download
	 */
	public static boolean isAutoDownloadEnabled;
	/**
	 * Setting value for sync-only-over-wifi
	 */
	public static boolean isOnlyWifi;
	
	// Current default owner_id(== default Google account)
	private static String DEFAULT_USER_ID = null;
	// Current default owner_auth_token(== default Google API access_token)
	private static String DEFAULT_USER_AUTH_TOKEN = null;
	
	private static int NUM_OF_USERS = 0;
	private static int NUM_OF_ITEMS = 0;
	private static float RATIO_OF_FTP_SYNC = 0;
	private static float RATIO_OF_CLOUD_SYNC = 0;
	private static float RATIO_OF_CENTRAL_SYNC = 0;
	
	
	/**
	 * Key for a versionName in default SharedPreferences
	 */
	public static final String SETTING_VERSION_KEY = "version";
	/**
	 * Key for a current user ID in default SharedPreferences
	 */
	public static final String SETTING_OWNER_ID_KEY = "ownersID";
	/** 
	 * Key for a current user ID's auth_token in default SharedPreferences
	 */
	public static final String SETTING_AUTH_TOKEN_KEY = "userToken";
	
	/**
	 * Sync time interval (30min)
	 */
	public static final long SYNC_INTERVAL = 30 * 60 * 1000;
	
	/**
	 * 	List of keys in default setting (application default SharedPreferences)
	 * 
	 *  BACKUP_MODE_KEY			: true/false
	 *  AUTO_DOWNLOAD_MODE_KEY  : true/false
	 *  WIFI_MODE_KEY			: true/false
	 *  RESPEAKING_MODE_KEY		: "phone"/"thumb"
	 */
	public static final String BACKUP_MODE_KEY = "backup_mode";
	/** */
	public static final String AUTO_DOWNLOAD_MODE_KEY = "autoDownload_mode";
	/** */
	public static final String WIFI_MODE_KEY = "only_wifi";
	/** */
	public static final String RESPEAKING_MODE_KEY = "respeaking_mode";


	/**
	 *  List of keys in each user's setting (SharedPreferences of emailAccount)
	 *  
	 *  APPROVED_RECORDING_KEY	: A set of recording's cloudID approved to be archived
	 *  [archive-approved recording-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_RECORDING_KEY	: A set of recording's cloudID to be downloaded from cloud
	 *  [download-approved recording-cloudID] : <archive-state>
	 *  ARCHIVED_RECORDING_KEY	: A set of recording IDs archived
	 *  
	 *  APPROVED_SPEAKERS_KEY	: A set of speaker's cloudID approved to be archived
	 *  [archive-approved Speaker-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_SPEAKERS_KEY	: A set of speaker's cloudID to be downloaded from cloud
	 *  [download-approved Speaker-cloudID] : <archive-state>
	 *  ARCHIVED_SPEAKERS_KEY	: A set of speaker IDs archived
	 *  
	 *  APPROVED_OTHERS_KEY	: A set of other file's cloudID approved to be archived
	 *  [archive-approved other-file-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_OTHERS_KEY : A set of other file's cloudID to be downloaded from cloud
	 *  [download-approved other-file-cloudID]  : <archive-state>
	 *  ARCHIVED_OTHERS_KEY	: A set of other files' IDs archived (transcript, mapping files)
	 */
	public static final String APPROVED_RECORDING_KEY = "approvedRecordings";
	/** */
	public static final String DOWNLOAD_RECORDING_KEY = "downloadRecordings";
	/** */
	public static final String ARCHIVED_RECORDING_KEY = "archivedRecordings";
	/** */
	public static final String APPROVED_SPEAKERS_KEY = "approvedSpeakers";
	/** */
	public static final String DOWNLOAD_SPEAKERS_KEY = "downloadSpeakers";
	/** */
	public static final String ARCHIVED_SPEAKERS_KEY = "archivedSpeakers";
	/** */
	public static final String APPROVED_OTHERS_KEY = "approvedOthers";
	/** */
	public static final String DOWNLOAD_OTHERS_KEY = "downloadOthers";
	/** */
	public static final String ARCHIVED_OTHERS_KEY = "archivedOthers";
	
	
	// Latest version name.
	private static final String DEFAULT_VERSION = "v01";
	
	
	/**
	 * Return current file-format version
	 * @return	String of version
	 */
	public static String getLatestVersion(){
		return DEFAULT_VERSION;
	}
	
	/**
	 * Return current default owner account
	 * @return	String of default owner account
	 */
	public static String getCurrentUserId() {
		return DEFAULT_USER_ID;
	}
	/**
	 * Set the default owner account
	 * @param Id	String of default owner account
	 */
	public static void setUserId(String Id) {
		DEFAULT_USER_ID = Id;
	}
	
	/**
	 * Return current default owner account's auth-token
	 * @return	String of default owner account's auth-token
	 */
	public static String getCurrentUserToken() {
		return DEFAULT_USER_AUTH_TOKEN;
	}
	/**
	 * Set the default owner account's auth-token
	 * @param token	String of default owner account's auth-token
	 */
	public static void setUserToken(String token) {
		DEFAULT_USER_AUTH_TOKEN = token;
	}
	
	/**
	 * Return current number of users
	 * @return	the number of users
	 */
	public static int getNumberOfUsers() {
		return NUM_OF_USERS;
	}
	/**
	 * Set the number of users
	 * @param num	the number of users
	 */
	public static void setNumberOfUsers(int num) {
		NUM_OF_USERS = num;
	}
	
	/**
	 * Return current number of items
	 * @return	the number of items
	 */
	public static int getNumberOfItems() {
		return NUM_OF_ITEMS;
	}
	/**
	 * Set the number of items
	 * @param num	the number of items
	 */
	public static void setNumberOfItems(int num) {
		NUM_OF_ITEMS = num;
	}
	
	/**
	 * Return current ftp upload ratio
	 * @return	the ratio of ftp-upload success
	 */
	public static float getFtpRatio() {
		return RATIO_OF_FTP_SYNC;
	}
	/**
	 * Set the current ftp upload ratio
	 * @param ratio		the ratio of ftp-upload success
	 */
	public static void setFtpRatio(float ratio) {
		RATIO_OF_FTP_SYNC = ratio;
	}
	
	/**
	 * Return current Cloud upload ratio
	 * @return	the ratio of Cloud-upload success
	 */
	public static float getCloudRatio() {
		return RATIO_OF_CLOUD_SYNC;
	}
	/**
	 * Set the current cloud upload ratio
	 * @param ratio		the ratio of cloud-upload success
	 */
	public static void setCloudRatio(float ratio) {
		RATIO_OF_CLOUD_SYNC = ratio;
	}
	
	/**
	 * Return current central upload ratio
	 * @return	the ratio of central-upload success
	 */
	public static float getCentralRatio() {
		return RATIO_OF_CENTRAL_SYNC;
	}
	/**
	 * Set the current central upload ratio
	 * @param ratio		the ratio of central-upload success
	 */
	public static void setCentralRatio(float ratio) {
		RATIO_OF_CENTRAL_SYNC = ratio;
	}
	
	/**
     * Return an scope for google-API scope
     * @return the scope of Google-Cloud
     */
    public static String getScope() {
    	String joiner = "";
		String scope = "oauth2:";
		for (String s: GoogleDriveStorage.getScopes()) {
			scope += joiner + s;
			joiner = " ";
		}

		for (String s: FusionIndex.getScopes()) {
			scope += joiner + s;
			joiner = " ";
		}
		return scope;
    }
}

