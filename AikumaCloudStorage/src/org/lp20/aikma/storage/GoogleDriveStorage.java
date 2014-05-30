package org.lp20.aikma.storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Implements DataStore interface using Google Drive.
 * 
 * @author haejoong
 */
public class GoogleDriveStorage implements DataStore {
	GoogleAuth gauth_;
	String authUrl_;     // authentication/authorization page url
	boolean gapiReady_;  // tells whether access token is obtained
	
	/**
	 * The object is not usable until authentication and authorization is done.
	 * In order to activate the object, the obtainAccessToken method must be
	 * called with a valid authorization code. For example,
	 * 
	 * 
	 * @param clientId Google API client ID
	 * @param clientSecret Google API client secret
	 */
	public GoogleDriveStorage(String clientId, String clientSecret) {
		gauth_ = new GoogleAuth(clientId, clientSecret);
		ArrayList<String> apis = new ArrayList<String>();
		apis.add("https://www.googleapis.com/auth/drive.file");
		authUrl_ = gauth_.getAuthUrl(apis);
		gapiReady_ = false;
	}
	
	@Override
	public InputStream load(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean store(String identifier, Data data) {
		// identifier - aikuma file path
		if (gapiReady_ == false)
			return false;
		JSONObject obj = gapi_insert(data);
		if (obj == null)
			return false;
		
		JSONObject meta = new JSONObject();
		meta.put("title", identifier);
		String fileid = (String) obj.get("id");
		JSONObject obj2 = gapi_update_metadata(fileid, meta);
		return obj2 != null;
	}

	public String getAuthUrl() {
		return authUrl_;
	}
	
	public boolean obtainAccessToken(String authCode) {
		gapiReady_ = gauth_.requestAccessToken(authCode);
		return gapiReady_;
	}
		
	private JSONObject gapi_insert(Data data) {
		try {
			URL url = new URL("https://www.googleapis.com/upload/drive/v2/files?uploadType=media");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", data.getMimeType());
			con.setRequestProperty("Authorization", "Bearer " + gauth_.getAccessToken());
			con.setChunkedStreamingMode(8192);
			Utils.copyStream(data.getInputStream(), con.getOutputStream(), false);

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;
			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			return null;
		}
	}
	
	private JSONObject gapi_update_metadata(String fileid, JSONObject obj) {
		try {
			String metajson = obj.toJSONString();
			URL url = new URL("https://www.googleapis.com/drive/v2/files/" + fileid);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setDoOutput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", String.valueOf(metajson.length()));
			con.setRequestProperty("Authorization", "Bearer " + gauth_.getAccessToken());
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(metajson);
			writer.flush();
			writer.close();

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			return null;
		}
	}
}
