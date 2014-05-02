package org.lp20.aikma.storage;

import java.io.InputStream;

public class GoogleDriveStorage implements DataStore {
	String clientId_;
	String clientSecret_;

	public GoogleDriveStorage(String clientId, String clientSecret) {
		clientId_ = clientId;
		clientSecret_ = clientSecret;
	}
	
	@Override
	public InputStream load(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void store(String identifier, InputStream data) {
		// identifier - aikuma file path
		
	}

	/**
	 * Returns a URL that the end user needs to visit to authenticate and authorize.
	 * In the page, user logs in to google and authorize APIs. Then, the page
	 * returns an authorization code.
	 * 
	 * @return A URL in string.
	 */
	public String getAuthUrl() {
		return "https://accounts.google.com/o/oauth2/auth?"
				+ "scope=https://www.googleapis.com/auth/drive.file&"
				+ "response_type=code&"
				+ "redirect_uri=urn:ietf:wg:oauth:2.0:oob&"
				+ "client_id=" + clientId_;
	}
	
	
	public void setAuthCode(String authCode) {
		/*
		"https//accounts.google.com/o/oauth2/token"
		+ "redirect_uri=urn:ietf:wg:oauth:2.0:oob&"
		+ "grant_type=authorization_code"
		+ "code=" + authCode + "&"
		+ "client_id=" + clientId_ + "&"
		+ 
		*/
	}
}
