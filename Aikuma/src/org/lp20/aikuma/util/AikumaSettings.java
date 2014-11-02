/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.util;

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
	 *  List of keys in default SharedPreferences
	 *  
	 *  APPROVED_RECORDING_KEY	: A set of recording's version and IDs approved to be archived
	 *  [archive-approved Recording Id]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  ARCHIVED_RECORDING_KEY	: A set of recording IDs archived
	 *  APPROVED_SPEAKERS_KEY	: A set of speaker IDs approved to be archived
	 *  [archive-approved Speaker Id]	: <Approved-date>|<archive-state>
	 *  ARCHIVED_SPEAKERS_KEY	: A set of speaker IDs archived
	 *  BACKUP_MODE_KEY			: true/false
	 *  AUTO_DOWNLOAD_MODE_KEY  : true/false
	 *  RESPEAKING_MODE_KEY		: "phone"/"thumb"
	 */
	public static final String APPROVED_RECORDING_KEY = "approvedRecordings";
	/** */
	public static final String ARCHIVED_RECORDING_KEY = "archivedRecordings";
	/** */
	public static final String APPROVED_SPEAKERS_KEY = "approvedSpeakers";
	/** */
	public static final String ARCHIVED_SPEAKERS_KEY = "archivedSpeakers";
	/** */
	public static final String BACKUP_MODE_KEY = "backup_mode";
	/** */
	public static final String AUTO_DOWNLOAD_MODE_KEY = "autoDownload_mode";
	/** */
	public static final String RESPEAKING_MODE_KEY = "respeaking_mode";

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
}

