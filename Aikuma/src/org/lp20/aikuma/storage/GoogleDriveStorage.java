package org.lp20.aikuma.storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static org.lp20.aikuma.storage.Utils.gapi_connect;

/**
 * Implementation of DataStore backed by Google Drive.
 * 
 * @author haejoong
 */
public class GoogleDriveStorage implements DataStore {
	String accessToken_;
	
	/**
	 * GoogleDriveStorage allows saving and retrieving data to and from the
	 * Google Drive. It requires an access token for the scopes specified by
	 * the {@code getScopes()} method. The access token can be obtained in
	 * different ways. The following example uses GoogleAuth class.
	 * 
	 * {@code
	 * GoogleAuth auth = new GoogleAuth(myClientId, myClientSecret);
	 * String authCode = get_auth_code_for_scopes(GoogleDriveStorage.getScopes());
	 * auth.requestAccessToken(authCode);
	 * GoogleDriveStorage gd = new GoogleDriveStorage(auth.getAccessToken());
	 * }
	 * 
	 * @param accessToken 
	 */
	public GoogleDriveStorage(String accessToken) {
		accessToken_ = accessToken;
	}
	
	@Override
	public InputStream load(String identifier) {
		String query = "trashed = false and title = \"" + identifier + "\"";
		JSONObject obj = gapi_list_files(query, null);
		JSONArray arr = (JSONArray) obj.get("items");
		if (arr == null || arr.size() == 0)
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
			if (arr != null) {
				for (Object item: arr) {
					JSONObject o = (JSONObject) item;
					String identifier = (String) o.get("title");
					String datestr = (String) o.get("modifiedDate");
					SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
					Date date;
					try {
						date = datefmt.parse(datestr);
					}
					catch (ParseException e) {
						date = null;
					}
					boolean cont = listItemHandler.processItem(identifier, date);
					if (cont == false)
						return;
				}
			}
			String nextPageToken = (String) obj.get("nextPageToken");
			if (nextPageToken != null)
				obj = gapi_list_files(null, nextPageToken);
			else
				obj = null;
		}
	}
	
	/**
	 * Returns a list of api scopes required to access files.
	 * 
	 * @return List of scopes.
	 */
	public static List<String> getScopes() {
		ArrayList<String> apis = new ArrayList<String>();
		apis.add("https://www.googleapis.com/auth/drive.file");
		return apis;
	}
	


	/**
	 * Upload a file.
	 * @param data
	 * @return JSONObject if successful, null otherwise.
	 */
	private JSONObject gapi_insert(Data data) {		
		try {
			URL url = new URL("https://www.googleapis.com/upload/drive/v2/files?uploadType=media");
			HttpURLConnection con = gapi_connect(url, "POST", accessToken_);
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
	
	/**
	 * Update metadata of an existing file.
	 * @param fileid
	 * @param obj
	 * @return JSONObject if successful, null otherwise.
	 */
	private JSONObject gapi_update_metadata(String fileid, JSONObject obj) {
		try {
			String metajson = obj.toJSONString();
			URL url = new URL("https://www.googleapis.com/drive/v2/files/" + fileid);
			HttpURLConnection con = gapi_connect(url, "PUT", accessToken_);
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
	
	/**
	 * List files.
	 * @param searchQuery
	 * @param pageToken
	 * @return JSONObject if successful, null otherwise.
	 */
	private JSONObject gapi_list_files(String searchQuery, String pageToken) {
		try {
			String base = "https://www.googleapis.com/drive/v2/files/";
			Utils.UrlBuilder ub = new Utils.UrlBuilder(base);
			if (pageToken != null && !pageToken.isEmpty())
				ub.addQuery("pageToken",  pageToken);
			else if (searchQuery != null && !searchQuery.isEmpty())
				ub.addQuery("q", searchQuery);
			HttpURLConnection con = gapi_connect(ub.toUrl(), "GET", accessToken_);
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Download a file.
	 * @param url
	 * @return InputStream if successful, null otherwise.
	 */
	private InputStream gapi_download(String url) {
		try {
			HttpURLConnection con = gapi_connect(new URL(url), "GET", accessToken_);
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;
			return con.getInputStream();
		}
		catch (IOException e) {
			return null;
		}
	}
}
