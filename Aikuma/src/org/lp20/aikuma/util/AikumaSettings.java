package org.lp20.aikuma.util;

public class AikumaSettings {

	// Name for preference 'setting'
	public static final String SETTING_NAME = "settings";
	
	// Current default owner_id(== default google account)
	private static String DEFAULT_OWNER_ID = "";

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
	public static String getCurrentOwnerId() {
		return DEFAULT_OWNER_ID;
	}
	/**
	 * Set the default owner account
	 * @param Id	String of default owner account
	 */
	public static void setOwnerId(String Id) {
		DEFAULT_OWNER_ID = Id;
	}
}
