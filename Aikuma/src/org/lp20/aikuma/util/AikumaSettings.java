/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.util;

/**
 * Class storing settings information
 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 *
 */
public class AikumaSettings {

	/**
	 * Key for a versionName in default SharedPreferences
	 */
	public static final String SETTING_VERSION_KEY = "version";
	/**
	 * Key for a current user ID in default SharedPreferences
	 */
	public static final String SETTING_OWNER_ID_KEY = "ownerID";
	
	// Current default owner_id(== default google account)
	private static String DEFAULT_USER_ID = null;
	private static int NUM_OF_USERS = 0;
	private static int NUM_OF_ITEMS = 0;
	private static float RATIO_OF_FTP_SYNC = 0;
	private static float RATIO_OF_CLOUD_SYNC = 0;
	private static float RATIO_OF_CENTRAL_SYNC = 0;

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