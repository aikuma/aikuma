/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.lp20.aikuma.model.ServerCredentials;
import org.lp20.aikuma.util.FileIO;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
/**
 * The embedded FTP server service
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class FTPServerService extends IntentService{

	private static final String TAG = FTPServerService.class.getCanonicalName();
	
	/**
	 * Broadcast message signature
	 */
	public final static String SERVER_RESULT = "org.lp20.aikuma.server.result";
	/**
	 * Key of data in the broadcast message
	 */
	public final static String SERVER_STATUS = "server_status";
	
	/**
	 * A list of keys used to start FTPServerService
	 * (Keys of actions in an incoming intent)
	 */
	public static final String ACTION_KEY = "id";
	/** */
	public static final String SERVER_ADDR_KEY = "serverAddress";
	
	private static FtpServer server;
	
	/**
	 * Constructor for IntentService subclasses
	 */
	public FTPServerService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String id = intent.getStringExtra(ACTION_KEY);
		Log.i(TAG, "receive: " + id + ", server: " + (server == null));
		if(id.equals("server")) {
			if(server != null) {
				return;
			}
			/*
			if(server != null) {
				Log.i(TAG, "server.suspend?" + server.isSuspended());
				if(server.isSuspended()) {
					Log.i(TAG, "server resume");
					server.resume();
				}
				return;
			}*/
			FtpServerFactory serverFactory = new FtpServerFactory();
			ListenerFactory listenerFactory = new ListenerFactory();
			listenerFactory.setPort(8888);
			
			PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();
			//userFactory.setFile(new File(FileIO.getAppRootPath(), "server.properties"));
			userFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
			UserManager userManager = userFactory.createUserManager();
			
			BaseUser user = new BaseUser();
			ServerCredentials credential = null;
			try {
				credential = ServerCredentials.read();
			} catch (IOException e1) {
				Log.e(TAG, "credential error: " + e1.getMessage());
			}
			if(credential == null) {
				user.setName("admin");
				user.setPassword("admin");
			} else {
				user.setName(credential.getUsername());
				user.setPassword(credential.getPassword());
			}
			user.setHomeDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
			
			List<Authority> authorities = new ArrayList<Authority>();
			authorities.add(new WritePermission());
			user.setAuthorities(authorities);
			
			try {
				userManager.save(user);
			} catch (FtpException e1) {
				Log.e(TAG, "user save error: " + e1.getMessage());
			}
			
			serverFactory.addListener("default", listenerFactory.createListener());
			serverFactory.setUserManager(userManager);
			server = serverFactory.createServer();
			
			try {
				server.start();
				Log.i(TAG, "server started: ");
				broadcastStatus("start");
			} catch (FtpException e) {
				Log.e(TAG, "server start error: " + e.getMessage());
			}	
		} else if(id.equals("stop")) {
			if(server != null && !server.isStopped()) {
				server.stop();
				server = null;
			}
		} else {
			String serverIP = intent.getStringExtra(SERVER_ADDR_KEY);
			
		}
		
	}
	
	private void broadcastStatus(String status) {
		Intent intent = new Intent(this.SERVER_RESULT);
		intent.putExtra(this.SERVER_STATUS, status);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

}
