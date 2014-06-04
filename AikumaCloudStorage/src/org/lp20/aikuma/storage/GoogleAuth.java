package org.lp20.aikuma.storage;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.simple.*;

/**
 * Utility class for handling google authentication/authorization.
 * 
 * @author haejoong
 */
public class GoogleAuth {
	private String clientId_;
	private String clientSecret_;
	private String lastError_;
	private String accessToken_;
	
	/**
	 * Construct an authentication/authorization object. Example,
	 * 
	 * - Call getAuthUrl(), and open an authentication/authorization page
	 *   pointed by the returned URL.
	 * - When submitted the form on the page, the browser displays a string
	 *   called authorization code.
	 * - Call requestAccessToken() method with the authorization code.
	 * - Call getAccessToken() method to get the access token.
	 * 
	 * @param clientId
	 * @param clientSecret
	 */
	public GoogleAuth(String clientId, String clientSecret) {
		clientId_ = clientId;
		clientSecret_ = clientSecret;
	}
	
	/**
	 * Returns a URL that the end user needs to visit to authenticate and authorize.
	 * In the page, user logs in to google and authorize APIs. Then, the page
	 * returns an authorization code.
	 * 
	 * @return A URL in string.
	 */
	public String getAuthUrl(List<String> apis) {
		String url = "https://accounts.google.com/o/oauth2/auth?"
				+ "response_type=code&"
				+ "redirect_uri=urn:ietf:wg:oauth:2.0:oob&"
				+ "client_id=" + clientId_;

		if (apis.size() > 0) {
			url += "&scope=";
			for (String api: apis) {
				url += api + " ";
			}
			url = url.trim();
		}
		
		return url;
	}

	/**
	 * Obtain an access token. Use getAccessToken() to get the obtained token.
	 * 
	 * @param authCode
	 * @return true if successful, false, otherwise
	 */
	public boolean requestAccessToken(String authCode) {
		try {
			URL url = new URL("https://accounts.google.com/o/oauth2/token");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write("redirect_uri=urn:ietf:wg:oauth:2.0:oob&");
			writer.write("grant_type=authorization_code&");
			writer.write("code=" + authCode + "&");
			writer.write("client_id=" + clientId_ + "&");
			writer.write("client_secret=" + clientSecret_);
			writer.flush();
			writer.close();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				accessToken_ = Utils.readStream(con.getInputStream());
				lastError_ = null;
				return true;
			} else {
				// TODO: Error logging?
				accessToken_ = null;
				lastError_ = "HTTP error: " + con.getResponseMessage();
				return false;
			}
		}
		catch (IOException e) {
			// TODO: Logging? e.printStackTrace();
			accessToken_ = null;
			lastError_ = "Exception: " + e.getMessage();
			return false;
		}
	}

	/**
	 * Return the access token obtained by requestAccessToken().
	 * Return null if there is nothing to return.
	 * 
	 * @return String token if token exists, null otherwise.
	 */
	public String getAccessToken() {
		if (accessToken_ != null) {
			JSONObject obj = (JSONObject) JSONValue.parse(accessToken_);
			return (String) obj.get("access_token");
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns an error message produced by a previous method call that
	 * is involved in communication with google auth servers.
	 * 
	 * @return Error message.
	 */
	public String getLastError() {
		return lastError_;
	}
}
