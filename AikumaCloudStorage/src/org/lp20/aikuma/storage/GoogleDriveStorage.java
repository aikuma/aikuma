package org.lp20.aikuma.storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final Logger log = Logger.getLogger(GoogleDriveStorage.class.getName());

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
		if (arr.size() == 0)
			return null;
		JSONObject item = (JSONObject) arr.get(0);
		String url = (String) item.get("downloadUrl");
		return gapi_download(url);
	}

	@Override
	public String store(String identifier, Data data) {
		// identifier - aikuma file path
		JSONObject obj = gapi_insert(data);
		if (obj == null) {
			log.log(Level.INFO, "gapi_intert returned null");
			return null;
                }
		
		JSONObject meta = new JSONObject();
		meta.put("title", identifier);
		String fileid = (String) obj.get("id");
		JSONObject obj2 = gapi_update_metadata(fileid, meta);
		if (obj2 != null)
			return (String) obj2.get("webContentLink");
		else
			return null;
	}

	@Override
	public boolean share(String identifier) {
		String name = "\"" + identifier.replaceAll("\"",  "\\\"") + "\"";
		JSONObject obj = gapi_list_files("trashed = false and title = " + name, null);
		
		if (obj == null)
			return false;  // "list" call failed
		
		String kind = (String) obj.get("kind");
		String fileid = null;
		if (kind.equals("drive#fileList")) {
			JSONArray arr = (JSONArray) obj.get("items");
			if (arr != null && arr.size() > 0) {
				JSONObject o = (JSONObject) arr.get(0);
				fileid = (String) o.get("id");
			}
		}
		
		if (fileid == null)
			return false;  // unexpected response type
		
		JSONObject r = gapi_make_public(fileid);
		if (r != null)
			return true;
		else
			return false;
	}
	
	@Override
	public void list(ListItemHandler listItemHandler) {
		JSONObject obj = gapi_list_files("trashed = false", null);
		while (obj != null) {
			log.log(Level.INFO, "processing list");
			JSONArray arr = (JSONArray) obj.get("items");
			for (Object item: arr) {
				JSONObject o = (JSONObject) item;
				String identifier = (String) o.get("title");
				String datestr = (String) o.get("modifiedDate");
				SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				Date date;
				try {
					date = datefmt.parse(datestr);
				}
				catch (ParseException e) {
					date = null;
				}
				boolean cont = listItemHandler.processItem(identifier, date);
				if (cont == false) {
					log.log(Level.INFO, "handler stopped iteration");
					return;
				}
			}
			String nextPageToken = (String) obj.get("nextPageToken");
			if (nextPageToken != null) {
				obj = gapi_list_files(null, nextPageToken);
			} else {
				obj = null;
				log.log(Level.INFO, "no more data to download");
			}
		}
		log.log(Level.INFO, "gapi_list_files returned null");
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

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.INFO, "upload request http respose: " + con.getResponseCode());
				log.log(Level.INFO, "response message: " + con.getResponseMessage());
				return null;
			}
			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.INFO, "IO exception: " + e.getMessage());
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
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.INFO, "gapi_list_files http response: " + con.getResponseCode());
				log.log(Level.INFO, "response messaage: " + con.getResponseMessage());
				return null;
			}
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.INFO, "IO error: " + e.getMessage());
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

	/**
	 * Make a file public.
	 * @param fileid String file id.
	 * @return json response from the server.
	 */
	private JSONObject gapi_make_public(String fileid)
	{
		try {
			JSONObject meta = new JSONObject();
			meta.put("type", "anyone");
			meta.put("role", "reader");
			String metajson = meta.toJSONString();
			
			URL url = new URL("https://www.googleapis.com/drive/v2/files/" + fileid + "/permissions");
			HttpURLConnection con = gapi_connect(url, "POST", accessToken_);
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
			System.out.println("exception");
			return null;
		}
	}
}
