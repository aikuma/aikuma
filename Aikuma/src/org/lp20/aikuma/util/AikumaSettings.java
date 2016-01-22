/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.util;

import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.google.GoogleDriveStorage;


/**
 * The class storing setting parameters which can be accessed
 * by all Android components in the application
 * (Global Parameters)
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AikumaSettings {
	/**
	 * Latest version name.
	 * 
	 * v02: 1. Recording-metadata: delete languages/speakers, add comments
	 * 		2. Speaker-metadata: delete languages, add comments/date
	 * 		3. Delete speaker-images
	 * 		4. Add recording-images
	 * 		5. New files: Tags (speaker, language, discourse-type, custom)
	 *
	 *      1. translation -> interpret, resepaking -> respeak
	 * 		2. mapping, transcript : TXT -> JSON
	 * 		3. deletion, archive files (?)
	 * 
	 * v03: 1. Flat file structure in cloud-storage
	 * 		2. Use of aikuma-cloud-library-0.10.1
	 * 		3. Remove two levels of hashes in local storage
	 * 		4. Admin-file structure in local storage
	 * 		5. Checksum field for recording
	 */
	private static final String DEFAULT_VERSION = "v03";	
	
	/**
	 * Setting value for backup
	 */
	public static boolean isBackupEnabled = true;
	/**
	 * Setting value for auto-download
	 */
	public static boolean isAutoDownloadEnabled = true;
	/**
	 * Setting value for sync-only-over-cellulars
	 */
	public static boolean isOnlyWifi;
	/**
	 * Setting value for sync-over-wifi
	 */
	public static boolean isNetwork;
	/**
	 * Setting value for public-share
	 */
	public static boolean isPublicShareEnabled;
	/**
	 * State value for cloud search
	 */
	public static boolean isSearchPossible;
	
	// Current default owner_id(== default Google account)
	private static String DEFAULT_USER_ID = null;
	// Current default owner_auth_token(== default Google API access_token)
	private static String DEFAULT_USER_AUTH_TOKEN = null;
	private static String DEFAULT_USER_ID_TOKEN = null;
	private static String DEFAULT_INDEX_SERVER = "https://aikuma.info:443/index";
	private static String DEFAULT_FILE_PROXY_SERVER = "https://aikuma.info:443";
	private static String DEFAULT_INDEX_SERVER_CLIENT_ID = "530026557211-46brcqr1p4ltlru6j6duc0am14oh83ei.apps.googleusercontent.com";
	/**
	 * Account of central fusion table
	 */
	public static String CENTRAL_USER_ID = "lp20.org@gmail.com";
	/**
	 * Temporary ID for all root-folder (TODO: might be changed depending on project folder structures)
	 */
	public static String ROOT_FOLDER_ID = "aikuma-" + DEFAULT_VERSION;
	
	private static int numOfUsers;
	private static int numOfSpeakers;
	private static int numOfItems;
	private static float ratioOfFtpSync;
	private static float ratioOfCloudSync;
	private static float ratioOfCentralSync;
	
	
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
	 * Key for current user ID's google ID token in default SharedPreferences
	 */
	public static final String SETTING_ID_TOKEN_KEY = "userIdToken";
	/**
	 * Key for a ratio of backup to Google Drive in default SharedPreferences
	 */
	public static final String SETTING_CLOUD_RATIO_KEY = "cloudSyncRatio";
	/**
	 * Key for a ratio of central-archive in default SharedPreferences
	 */
	public static final String SETTING_CENTRAL_RATIO_KEY = "centralSyncRatio";
	/**
	 * Key for index server url
	 */
	public static final String SETTING_INDEX_SERVER_URL = "indexServerUrl";
	
	/**
	 * Sync time interval (30min)
	 */
	public static final long SYNC_INTERVAL = 30 * 60 * 1000;
	
	/**
	 * Extra audio recording duration after a thumb is lifted or head becomes far away
	 */
	public static final int EXTRA_AUDIO_DURATION = 250;
	
	/**
	 * Default rewind time when creating a derivative recording (in msec)
	 */
	public static final int DEFAULT_RESPEAK_REWIND = 300;
	
	/**
	 * Recording sampleRate
	 */
	public static final long DEFAULT_SAMPLERATE = 16000l;
	
	
	/**
	 * 	List of keys in default setting (application default SharedPreferences)
	 * 
	 *  BACKUP_MODE_KEY			: true/false
	 *  AUTO_DOWNLOAD_MODE_KEY  : true/false
	 *  WIFI_MODE_KEY			: true/false
	 *  CELLULAR_MODE_KEY		: true/false
	 *  PUBLIC_SHARE_CONSENT_KEY: true/false
	 *  RESPEAKING_MODE_KEY		: "thumb"
	 *  
	 *  SEARCH_STATE_KEY		: true/false
	 */
	public static final String BACKUP_MODE_KEY = "backup_mode";
	/** */
	public static final String AUTO_DOWNLOAD_MODE_KEY = "autoDownload_mode";
	/** */
	public static final String WIFI_MODE_KEY = "only_wifi";
	/** */
	public static final String CELLULAR_MODE_KEY = "allow_cellular";
	/** */
	public static final String PUBLIC_SHARE_CONSENT_KEY = "public_share";
	/** */
	public static final String RESPEAKING_MODE_KEY = "respeaking_mode";

	/** */
	public static final String SEARCH_STATE_KEY = "search_possible";
	

	/**
	 *  List of keys in each user's setting (SharedPreferences of emailAccount)
	 *  
	 *  APPROVED_RECORDING_KEY	: A set of recording's cloudID approved to be archived
	 *  [upload/archive-approved recording-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_RECORDING_KEY	: A set of recording's cloudID to be downloaded from cloud
	 *  UPLOAD_RECORDING_KEY	: A set of recording's cloudID to be uploaded
	 *  [download-approved recording-cloudID] : <archive-state>
	 *  ARCHIVED_RECORDING_KEY	: A set of recording IDs archived
	 *  
	 *  APPROVED_SPEAKERS_KEY	: A set of speaker's cloudID approved to be archived
	 *  [upload/archive-approved Speaker-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_SPEAKERS_KEY	: A set of speaker's cloudID to be downloaded from cloud
	 *  UPLOAD_SPEAKERS_KEY		: A set of speaker's cloudID to be uploaded
	 *  [download-approved Speaker-cloudID] : <archive-state>
	 *  ARCHIVED_SPEAKERS_KEY	: A set of speaker IDs archived
	 *  
	 *  APPROVED_OTHERS_KEY	: A set of other file's cloudID approved to be archived
	 *  [upload/archive-approved other-file-cloudID]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  DOWNLOAD_OTHERS_KEY : A set of other file's cloudID to be downloaded from cloud
	 *  UPLOAD_OTHERS_KEY	: A set of other file's cloudID to be uploaded
	 *  [download-approved other-file-cloudID]  : <archive-state>
	 *  ARCHIVED_OTHERS_KEY	: A set of other files' IDs archived (transcript, mapping files)
	 */
	public static final String APPROVED_RECORDING_KEY = "approvedRecordings";
	/** */
	public static final String DOWNLOAD_RECORDING_KEY = "downloadRecordings";
	/** */
	public static final String UPLOAD_RECORDING_KEY = "uploadRecordings";
	/** */
	public static final String ARCHIVED_RECORDING_KEY = "archivedRecordings";
	/** */
	public static final String APPROVED_SPEAKERS_KEY = "approvedSpeakers";
	/** */
	public static final String DOWNLOAD_SPEAKERS_KEY = "downloadSpeakers";
	/** */
	public static final String UPLOAD_SPEAKERS_KEY = "uploadSpeakers";
	/** */
	public static final String ARCHIVED_SPEAKERS_KEY = "archivedSpeakers";
	/** */
	public static final String APPROVED_OTHERS_KEY = "approvedOthers";
	/** */
	public static final String DOWNLOAD_OTHERS_KEY = "downloadOthers";
	/** */
	public static final String UPLOAD_OTHERS_KEY = "uploadOthers";
	/** */
	public static final String ARCHIVED_OTHERS_KEY = "archivedOthers";
	/** */
	public static final String SOURCE_LANG_BUFFER_KEY = "sourceLanguages";
	/** */
	public static final String INTERPRET_LANG_BUFFER_KEY = "interpretLanguages";
	
	
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
	 * Return current default owner account's ID token.
	 * @return	String of default owner account's ID token.
	 */
	public static String getCurrentUserIdToken() {
		return DEFAULT_USER_ID_TOKEN;
	}

	/**
	 * Set the default owner account's ID token.
	 * @param token	String of default owner account's ID token.
	 */
	public static void setUserIdToken(String token) {
		DEFAULT_USER_ID_TOKEN = token;
	}

	/**
	 * Return current number of users
	 * @return	the number of users
	 */
	public static int getNumberOfUsers() {
		return numOfUsers;
	}
	/**
	 * Set the number of users
	 * @param num	the number of users
	 */
	public static void setNumberOfUsers(int num) {
		numOfUsers = num;
	}
	
	/**
	 * Return current number of speakers
	 * @return	the number of speakers
	 */
	public static int getNumberOfSpeakers() {
		return numOfSpeakers;
	}
	
	/**
	 * Set the number of speakers
	 * @param num	the number of speakers
	 */
	public static void setNumberOfSpeakers(int num) {
		numOfSpeakers = num;
	}
	
	/**
	 * Return current number of items
	 * @return	the number of items
	 */
	public static int getNumberOfItems() {
		return numOfItems;
	}
	/**
	 * Set the number of items
	 * @param num	the number of items
	 */
	public static void setNumberOfItems(int num) {
		numOfItems = num;
	}
	
	/**
	 * Return current ftp upload ratio
	 * @return	the ratio of ftp-upload success
	 */
	public static float getFtpRatio() {
		return ratioOfFtpSync;
	}
	/**
	 * Set the current ftp upload ratio
	 * @param ratio		the ratio of ftp-upload success
	 */
	public static void setFtpRatio(float ratio) {
		ratioOfFtpSync = ratio;
	}
	
	/**
	 * Return current Cloud upload ratio
	 * @return	the ratio of Cloud-upload success
	 */
	public static float getCloudRatio() {
		return ratioOfCloudSync;
	}
	/**
	 * Set the current cloud upload ratio
	 * @param ratio		the ratio of cloud-upload success
	 */
	public static void setCloudRatio(float ratio) {
		ratioOfCloudSync = ratio;
	}
	
	/**
	 * Return current central upload ratio
	 * @return	the ratio of central-upload success
	 */
	public static float getCentralRatio() {
		return ratioOfCentralSync;
	}
	/**
	 * Set the current central upload ratio
	 * @param ratio		the ratio of central-upload success
	 */
	public static void setCentralRatio(float ratio) {
		ratioOfCentralSync = ratio;
	}

	/**
	 * Return the URL of the index server.
	 * @return  the URL of the index server.
	 */
	public static String getIndexServerUrl() {
	    return DEFAULT_INDEX_SERVER;
	}
	
	/**
	 * Return the URL of the file-proxy server
	 * @return	the URL of the file-proxy server
	 */
	public static String getFileProxyServerUrl() {
		return DEFAULT_FILE_PROXY_SERVER;
	}

	/**
	 * Set the URL of the index server.
	 * @param url	URL of central index-server
	 */
	public static void setIndexServerUrl(String url) {
	    DEFAULT_INDEX_SERVER = url;
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

    /**
     * Return a scope for a client ID of web-application
     * @return	The scope for the web-application(central index-server)
     */
	public static String getIdTokenScope() {
	    return "audience:server:client_id:" + DEFAULT_INDEX_SERVER_CLIENT_ID;
	}
}

