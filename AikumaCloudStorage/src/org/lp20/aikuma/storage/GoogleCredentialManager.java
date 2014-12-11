package org.lp20.aikuma.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.lp20.aikuma.storage.GoogleAuth;

public class GoogleCredentialManager {
	String credentialFile_;
	Properties cred_;
	GoogleAuth auth_;
	
	/**
	 * Constructor a new credentials manager.
	 * 
	 * @param filename Java properties file containing the following fields.
	 *	- access-token
	 *  - refresh-token
	 *  - client-id
	 *  - client-secret
	 */
	public GoogleCredentialManager(String filename) {
		credentialFile_ = filename;
		cred_ = new Properties();
		if (!loadCredentials())
			throw new RuntimeException("failed to read credentials file: " + filename);
		
		if (!GoogleAuth.validateAccessToken(cred_.getProperty("access-token"))) {
			if (renewAccessToken() == null) {
				throw new RuntimeException("can't renew expired access token");
			}
		}
	}
	
	/**
	 * Get access token from the credential file.
	 * 
	 * @return Access token, or null on failure.
	 */
	public String getAccessToken() {
		return cred_.getProperty("access-token");
	}
	
	/**
	 * Renew access token.
	 * 
	 * @return Access token, or null on failure.
	 */
	public String renewAccessToken() {
		GoogleAuth auth = new GoogleAuth(
				cred_.getProperty("client-id"),
				cred_.getProperty("client-secret")
		);
		String rt = cred_.getProperty("refresh-token");
		if (auth.refreshAccessToken(rt)) {
			cred_.setProperty("access-token", auth.getAccessToken());
			rt = auth.getRefreshToken();
			if (rt != null)
				cred_.setProperty("refresh-token", rt);
			return cred_.getProperty("access-token");
		}
		else {
			return null;
		}
	}
	
	private boolean loadCredentials() {
		File file = new File(credentialFile_);
		if (!file.exists() || !file.isFile() || !file.canRead())
			return false;
		try {
			cred_.load(new FileInputStream(file));
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
