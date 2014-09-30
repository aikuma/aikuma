/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
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
	 *  Google API access_token shared by all Android components
	 */
	public static String googleAuthToken;
	
	/**
	 * Setting value for backup
	 */
	public static boolean isBackupEnabled;
	/**
	 * Setting value for auto-download
	 */
	public static boolean isAutoDownloadEnabled;
	
	/**
	 *  List of keys in default SharedPreferences
	 *  
	 *  ARCHIVE_RECORDING_KEY	: A set of recording IDs approved to be archived
	 *  [archivedRecording Id]	: <Approved-date>|<archive-state>|<data-store-uri>
	 *  GOOGLE_AUTH_TOKEN_KEY
	 *  GOOGLE_ACCOUNT_KEY
	 *  BACKUP_MODE_KEY			: true/false
	 *  AUTO_DOWNLOAD_MODE_KEY  : true/false
	 *  RESPEAKING_MODE_KEY		: "phone"/"thumb"
	 */
	public static final String ARCHIVE_RECORDING_KEY = "approvedRecordings";
	/** */
	public static final String GOOGLE_AUTH_TOKEN_KEY = "googleAuthToken";
	/** */
	public static final String GOOGLE_ACCOUNT_KEY = "defaultGoogleAccount";
	/** */
	public static final String BACKUP_MODE_KEY = "backup_mode";
	/** */
	public static final String AUTO_DOWNLOAD_MODE_KEY = "autoDownload_mode";
	/** */
	public static final String RESPEAKING_MODE_KEY = "respeaking_mode";

}
