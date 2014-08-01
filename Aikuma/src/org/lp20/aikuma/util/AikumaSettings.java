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
	 *  Setting key for recordings which are approved to be archived
	 */
	public static String archivingRecordingKey = "approvedRecordings";
	
	
}
