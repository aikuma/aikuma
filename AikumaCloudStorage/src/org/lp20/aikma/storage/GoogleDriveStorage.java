package org.lp20.aikma.storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Implements DataStore interface using Google Drive.
 * 
 * @author haejoong
 */
public class GoogleDriveStorage implements DataStore {
	String accessToken_;
	
	/**
	 * The object is not usable until authentication and authorization is done.
	 * In order to activate the object, the obtainAccessToken method must be
	 * called with a valid authorization code. For example,
	 * 
	 * 
	 * @param clientId Google API client ID
	 * @param clientSecret Google API client secret
	 */
	public GoogleDriveStorage(String accessToken) {
		accessToken_ = accessToken;
	}
	
	@Override
	public InputStream load(String identifier) {
		String query = "trashed = false and title = \"" + identifier + "\"";
		JSONObject obj = gapi_list_files(query, null);
		JSONArray arr = (JSONArray) obj.get("items");
		if (arr.size() == 0)
			return null;
		JSONObject item = (JSONObject) arr.get(0);
		String url = (String) item.get("downloadUrl");
		return gapi_download(url);
	}

	@Override
	public boolean store(String identifier, Data data) {
		// identifier - aikuma file path
		JSONObject obj = gapi_insert(data);
		if (obj == null)
			return false;
		
		JSONObject meta = new JSONObject();
		meta.put("title", identifier);
		String fileid = (String) obj.get("id");
		JSONObject obj2 = gapi_update_metadata(fileid, meta);
		return obj2 != null;
	}

	@Override
	public void list(ListItemHandler listItemHandler) {
		JSONObject obj = gapi_list_files("trashed = false", null);
		while (obj != null) {
			JSONArray arr = (JSONArray) obj.get("items");
			for (Object item: arr) {
				String identifier = (String) ((JSONObject) item).get("title");
				listItemHandler.processItem(identifier);
			}
			String nextPageToken = (String) obj.get("nextPageToken");
			if (nextPageToken != null)
				obj = gapi_list_files(null, nextPageToken);
			else
				obj = null;
		}
	}
	
	public static List<String> getScopes() {
		ArrayList<String> apis = new ArrayList<String>();
		apis.add("https://www.googleapis.com/auth/drive.file");
		return apis;
	}
	
	private HttpURLConnection gapi_connect(URL url, String method) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(true);
		con.setDoOutput(true);
		con.setRequestMethod(method);
		con.setRequestProperty("Authorization", "Bearer " + accessToken_);
		return con;
	}
	
	private JSONObject gapi_insert(Data data) {		
		try {
			URL url = new URL("https://www.googleapis.com/upload/drive/v2/files?uploadType=media");
			HttpURLConnection con = gapi_connect(url, "POST");
			con.setRequestProperty("Content-Type", data.getMimeType());
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
			HttpURLConnection con = gapi_connect(url, "PUT");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", String.valueOf(metajson.length()));
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
	
	private JSONObject gapi_list_files(String searchQuery, String pageToken) {
		try {
			String base = "https://www.googleapis.com/drive/v2/files/";
			Utils.UrlBuilder ub = new Utils.UrlBuilder(base);
			if (pageToken != null && !pageToken.isEmpty())
				ub.addQuery("pageToken",  pageToken);
			else if (searchQuery != null && !searchQuery.isEmpty())
				ub.addQuery("q", searchQuery);
			HttpURLConnection con = gapi_connect(ub.toUrl(), "GET");
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			return null;
		}
	}
	
	private InputStream gapi_download(String url) {
		try {
			HttpURLConnection con = gapi_connect(new URL(url), "GET");
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;
			return con.getInputStream();
		}
		catch (IOException e) {
			return null;
		}
	}
}
