package org.lp20.aikuma.storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.NoSuchElementException;
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

	static final String DSVER_FIELD = "aikuma_ds_version";
	static final String DSVER = "v01";
	static final String ROOT_FIELD = "aikuma_root_id";
	static final String PREFIX_FIELD = "aikuma_prefix";
	static final String ROOT_FILE = "aikuma_root_id.txt";
	static final String FOLDER_MIME = "application/vnd.google-apps.folder";

	Map<String,String> mDirs;
	String mAccessToken;
	String mRootId;
	String mCentralEmail;
	
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
	 * @param accessToken Access token for a google drive account.
	 * @param rootId Globally unique identifier for the root directory.
	 * @param centeralEmail Email address to share stored files with.
	 *		Used by share() method. Shared files become public by
	 *		the google drive corresponding to the email address.
	 */
	public GoogleDriveStorage(
			String accessToken,
			String rootId,
			String centralEmail)
		throws DataStore.StorageException {
		mAccessToken = accessToken;
		mRootId = rootId;
		mCentralEmail = centralEmail;
		mDirs = new HashMap<String,String>();

		initialize_aikuma_folder();
	}
	
	@Override
	public InputStream load(String identifier) {
		String query = String.format(
				"trashed = false" +
				" and title = '%s'" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
				escape_quote(basename(identifier)),
				ROOT_FIELD, escape_quote(mRootId),
				PREFIX_FIELD, escape_quote(dirname(identifier)));

		JSONObject obj = gapi_list_files(query, null);

		if (obj == null) return null;
		JSONArray arr = (JSONArray) obj.get("items");
		if (arr.size() == 0)
			return null;
		JSONObject item = (JSONObject) arr.get(0);
		String url = (String) item.get("downloadUrl");
		return gapi_download(url);
	}

	@Override
	public String store(String identifier, Data data) {
		File f = new File(normpath(identifier));
		String parentId = mkdir(f.getParent());

		JSONObject meta = new JSONObject();
		meta.put("title", f.getName());

		JSONArray parents = new JSONArray();
		JSONObject parent = new JSONObject();
		parent.put("id", parentId);
		parents.add(parent);
		meta.put("parents", parents);
		meta.put("properties", getProp(mRootId, f.getPath()));

		JSONObject obj = gapi_insert2(data, meta);
		if (obj == null) return null;

		return (String) obj.get("id");
	}

	@Override
	public boolean share(String identifier) {
		String query = String.format(
				"trashed = false" +
				" and title = '%s'" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
				escape_quote(basename(identifier)),
				ROOT_FIELD, escape_quote(mRootId),
				PREFIX_FIELD, escape_quote(dirname(identifier)));

		JSONObject obj = gapi_list_files(query, null);
		
		if (obj == null) return false;  // "list" call failed
		
		String fileid = null;
		JSONArray arr = (JSONArray) obj.get("items");
		if (arr != null && arr.size() > 0) {
			JSONObject o = (JSONObject) arr.get(0);
			fileid = (String) o.get("id");
		} else {
			log.log(Level.FINE, "no file by identifier: " + identifier);
			return false;
		}
		
		JSONObject r = gapi_share_with(fileid, mCentralEmail);
		return r != null;
	}
	
	@Override
	public void list(ListItemHandler listItemHandler) {
		String query = String.format(
				"trashed = false" +
				" and mimeType != '" + FOLDER_MIME + "'" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
				ROOT_FIELD,
				mRootId.replaceAll("'", "\\'"));

		for (Search e = search(query); e.hasMoreElements();) {
			JSONObject o;
			try {
				o = e.nextElement();
			} catch (Search.Error err) {
				// TODO: need a way to pass exception to client
				log.log(Level.FINE, "search failed");
				return;
			}
			String identifier;
			Map<String,String> props = props_to_map(o.get("properties"));
			if (props.containsKey(PREFIX_FIELD))
				identifier = joinpath(props.get(PREFIX_FIELD), (String) o.get("title"));
			else
				identifier = "UNKNOWN";
			String datestr = (String) o.get("modifiedDate");
			SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			Date date;
			try {
				date = datefmt.parse(datestr.replaceAll("Z$"," GMT"));
			} catch (ParseException err) {
				date = null;
			}
			boolean cont = listItemHandler.processItem(identifier, date);
			if (cont == false)
				return;
		}
	}
	
	/**
	 * Returns a list of api scopes required to access files.
	 * 
	 * @return List of scopes.
	 */
	public static List<String> getScopes() {
		ArrayList<String> apis = new ArrayList<String>();
		apis.add("https://www.googleapis.com/auth/drive");
		return apis;
	}
	


	private String normpath(String path) {
		return path.replaceAll("^/*", "/");
	}

	private String joinpath(String p1, String p2) {
		return p1.replaceAll("/*$","") + "/" + p2.replaceAll("^/*","");
	}

	private String dirname(String path) {
		String p = (new File(normpath(path))).getParent();
		return p == null ? "" : p;
	}

	private String basename(String path) {
		return (new File(normpath(path))).getName();
	}

	private String escape_quote(String s) {
		return s.replaceAll("'", "\\'");
	}

	private class Properties extends JSONArray {
		public void put(String key, String value) {
			JSONObject obj = new JSONObject();
			obj.put("key", key);
			obj.put("value", value);
			obj.put("visibility", "PUBLIC");
			add(obj);
		}
	}

	private Properties getProp(String rootid, String path) {
		Properties props = new GoogleDriveStorage.Properties();
		props.put(DSVER_FIELD, DSVER);
		props.put(ROOT_FIELD, rootid);
		props.put(PREFIX_FIELD, dirname(path));
		return props;
	}

	private abstract class Search {

		String mNextPageToken;
		int mNumItems;
		int mIdx;
		JSONArray mArr;
		String mKind;
		String mQuery;
		boolean mErr;

		public class Error extends Exception {}

		public Search(String query, String kind) {
			mKind = kind;
			mQuery = query;
			mNextPageToken = null;
			mNumItems = 0;
			mIdx = 0;
			mErr = false;
			processListObj(getMore(mQuery, mNextPageToken));
		}

		protected abstract JSONObject getMore(String query, String pageToken);

		public boolean hasMoreElements() {
			// if there is error, let them call nextElement() and handle error
			return mErr || mNextPageToken != null || mNumItems > mIdx;
		}

		public JSONObject nextElement() throws NoSuchElementException, Error {
			if (mErr)
				throw new Error();
			if (mNumItems <= mIdx && mNextPageToken != null)
				processListObj(getMore(mQuery, mNextPageToken));
			if (mNumItems > mIdx)
				return (JSONObject) mArr.get(mIdx++);
			else
				throw new NoSuchElementException();
		}

		void processListObj(JSONObject obj) {
			if (obj == null) {
				log.log(Level.FINE, "received null");
				mNextPageToken = null;
				mNumItems = 0;
				mIdx = 0;
				mErr = true;
			} else if (!mKind.equals((String) obj.get("kind"))) {
				log.log(Level.FINE, "wrong kind received: " + (String) obj.get("kind"));
				mErr = true;
			} else {
				mArr = (JSONArray) obj.get("items");
				mIdx = 0;
				mNumItems = mArr.size();
				mNextPageToken = (String) obj.get("nextPageToken");
				log.log(Level.FINE, "items: " + mNumItems + " next token: " + mNextPageToken);
			}
		}
	}

	private Search search(String query) {
		return new Search(query, "drive#fileList") {
			@Override
			protected JSONObject getMore(String query, String pageToken) {
				log.log(Level.FINE, "pageToken: " + pageToken + " query: " + query);
				return gapi_list_files(query, pageToken);
			}
		};
	}
	
	private void initialize_aikuma_folder() throws DataStore.StorageException {
		String query = String.format(
				"trashed = false " +
				" and mimeType='%s'" +
				" and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
				FOLDER_MIME,
				ROOT_FIELD,
				escape_quote(mRootId));
		
		Search e = search(query);
		if (e.hasMoreElements()) {
			log.log(Level.FINE, "found aikumafied folders");
			for (; e.hasMoreElements();) {
				JSONObject o;
				try {
					o = e.nextElement();
				} catch (Search.Error err) {
					log.log(Level.FINE, "search exception");
					throw new DataStore.StorageException();
				}
				Map<String,String> props = props_to_map(o.get("properties"));
				String path;
				String prefix = props.get(PREFIX_FIELD);
				if ("".equals(prefix))
					path = "/";
				else
					path = joinpath(prefix, (String) o.get("title"));
				String fileid = (String) o.get("id");
                                log.log(Level.FINE, "folder: " + fileid + " " + path);
				if (path == null) {
					log.log(Level.WARNING, "non-aikumafied directory found: " + (String) o.get("title"));
				} else if (mDirs.containsKey(path)) {
					log.log(Level.SEVERE, "multiple folders with same path found: " + path);
					throw new DataStore.StorageException();
				} else {
					mDirs.put(path, fileid);
				}
			}
			if (!mDirs.containsKey("/")) {
				log.log(Level.FINE, "no aikumafied root found -- falling back");
				mDirs.clear();
				initialize_aikuma_folder2();
			}

		} else {
			log.log(Level.FINE, "didn't find aikumafied dir structure, falling back");
			initialize_aikuma_folder2();
		}
	}

	private void initialize_aikuma_folder2() throws DataStore.StorageException {
		String query = String.format(
				"trashed = false and title = '%s'" +
				" and mimeType != '%s'",
				ROOT_FILE,
				FOLDER_MIME);

		Search e = search(query);
		if (e.hasMoreElements()) {
			ArrayList<String> parentIds = new ArrayList<String>();
			for (; e.hasMoreElements();) {
				JSONObject o;
				try {
					o = e.nextElement();
				} catch (Search.Error err) {
					log.log(Level.FINE, "search exception");
					throw new DataStore.StorageException();
				}

				log.log(Level.FINE, "found " + ROOT_FILE + ", checking...");
				InputStream is = gapi_download((String) o.get("downloadUrl"));
				if (is == null) {
					log.log(Level.FINE, "download failed");
					throw new DataStore.StorageException();
				}
				String id;
				try {
					id = Utils.readStream(is).trim();
				} catch (IOException err) {
					log.log(Level.FINE, "error: " + err.getMessage());
					throw new DataStore.StorageException();
				}
				if (id.equals(mRootId)) {
					for (Object p: (JSONArray) o.get("parents")) {
						String pid = (String) ((JSONObject) p).get("id");
						parentIds.add(pid);
					}
				}
			}
			if (parentIds.size() == 0) {
				log.log(Level.FINE, "failed to identify root folder containing the root id file");
				initialize_aikuma_folder3();
			} else if (parentIds.size() == 1) {
				log.log(Level.FINE, "found manually created aikuma folder");
				aikumafy(parentIds.get(0));
			} else {
				log.log(Level.FINE, "multiple Aikuma folders matching root id: " + mRootId);
				throw new DataStore.StorageException();
			}
		} else {
			initialize_aikuma_folder3();
		}
	}	

	private void initialize_aikuma_folder3() throws DataStore.StorageException {
		log.log(Level.FINE, "creating a new root");
		JSONObject meta = new JSONObject();
		meta.put("properties", getProp(mRootId, "/"));
		meta.put("title", "aikuma");
                meta.put("mimeType", FOLDER_MIME);
		JSONObject res = gapi_make_file(meta);
                if (res == null)
                    throw new DataStore.StorageException();
		String fid = (String) res.get("id");
		mDirs.put("/", fid);
	}

	private void aikumafy(String fileid) throws DataStore.StorageException {
		log.log(Level.FINE, "aikumafying folder: " + fileid);
		Stack<String> stack = new Stack<String>();
		stack.push(fileid);
		stack.push("/");

		String q = "trashed = false and '%s' in parents and mimeType='" + FOLDER_MIME + "'";
		while (!stack.empty()) {
			String base = stack.pop();
			String pid = stack.pop();
			mDirs.put(base, pid);
			aikumafy_update_properties(pid, base);
			for (Search e = search(String.format(q,pid)); e.hasMoreElements();) {
				JSONObject c;
				try {
					c = e.nextElement();
				} catch (Search.Error err) {
					log.log(Level.FINE, "search exception");
					throw new DataStore.StorageException();
				}
				String fid = (String) c.get("id");
				String name = (String) c.get("title");
				stack.push(fid);
				stack.push(normpath(joinpath(base, name)));
			}
		}

		q = "trashed=false and '%s' in parents and mimeType!='" + FOLDER_MIME + "'";
		for (Object obj:  mDirs.keySet().toArray()) {
			String path = (String) obj;
			String pid = mDirs.get(path);
			for (Search e = search(String.format(q,pid)); e.hasMoreElements();)
				try {
					aikumafy_update_properties(e.nextElement(), path);
				} catch (Search.Error err) {
					log.log(Level.FINE, "search exception");
					throw new DataStore.StorageException();
				}
		}
	}

	private void aikumafy_update_properties(JSONObject child, String basedir) {
		String fid = (String) child.get("id");
		String name = (String) child.get("title");
		String p = joinpath(basedir, name);
		aikumafy_update_properties(fid, p);
	}

	private void aikumafy_update_properties(String fileid, String path) {
		log.log(Level.FINE, "updating: " + path);
		JSONObject meta = new JSONObject();
		if (path != "/") {
			File f = new File(path);
			String parentId = mkdir(f.getParent());
			JSONArray parents = new JSONArray();
			JSONObject parent = new JSONObject();
			parent.put("id", parentId);
			parents.add(parent);
			meta.put("parents", parents);
			meta.put("title", f.getName());
		}
		meta.put("properties", getProp(mRootId, path));
                log.log(Level.FINE, "setting properties for " + fileid + ": " + meta.toString());
		gapi_update_metadata(fileid, meta);
	}

	private Map<String,String> props_to_map(Object props) {
		HashMap<String,String> h = new HashMap<String,String>();
		for (Object p: (JSONArray) props) {
			JSONObject obj = (JSONObject) p;
			h.put((String) obj.get("key"), (String) obj.get("value"));
		}
		return h;
	}

	private String mkdir(String path) {
		File f = new File(path==null ? "/" : path);
		String fid = mDirs.get(f.getPath());
		if (fid == null) {
			String parentFid = mkdir(f.getParent());

			JSONObject meta = new JSONObject();
			meta.put("properties", getProp(mRootId, f.getPath()));
			meta.put("title", f.getName());
			meta.put("mimeType", FOLDER_MIME);

			JSONArray parents = new JSONArray();
			JSONObject parent = new JSONObject();
			parent.put("id", parentFid);
			parents.add(parent);
			meta.put("parents", parents);

			JSONObject res = gapi_make_file(meta);
			fid = (String) res.get("id");
			mDirs.put(f.getPath(), fid);
		}
		return fid;
	}

	/**
	 * Create an empty file or a folder with given metadata.
	 * @param meta
	 * @return JSONObject if succeeds, null, otherwise.
	 */
        private JSONObject gapi_make_file(JSONObject meta) {
		try {
			String metajson = meta.toString();
			URL url = new URL("https://www.googleapis.com/drive/v2/files");
			HttpURLConnection con = gapi_connect(url, "POST", mAccessToken);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", String.valueOf(metajson.length()));
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(metajson);
                        writer.flush();
                        writer.close();

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, "http respose: " + con.getResponseCode() + " " + con.getResponseMessage());
				return null;
			}
			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.FINE, "IO exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Create a new file with given content and metadata.
	 * @param data
	 * @param meta
	 * @return JSONObject if succeeds, null, otherwise.
	 */
	private JSONObject gapi_insert2(Data data, JSONObject meta) {
		log.log(Level.FINE, "metadata: " + meta.toString());
		try {
			URL url = new URL("https://www.googleapis.com/upload/drive/v2/files?uploadType=multipart");
			HttpURLConnection con = gapi_connect(url, "POST", mAccessToken);
			String bd = "bdbdbdbdbdbdbdbdbdbd";
			con.setRequestProperty("Content-Type", "multipart/related; boundary=\"" + bd + "\"");
			con.setChunkedStreamingMode(8192);
			OutputStream os = con.getOutputStream();

			os.write(("--" + bd + "\r\n").getBytes());
			os.write("Content-Type: application/json\r\n".getBytes());
			os.write("\r\n".getBytes());
			os.write(meta.toString().getBytes());
			os.write("\r\n".getBytes());

			os.write(("--" + bd + "\r\n").getBytes());
			os.write(("Content-Type: " + data.getMimeType() + "\r\n").getBytes());
			os.write("\r\n".getBytes());
			Utils.copyStream(data.getInputStream(), os, false);
			os.write("\r\n".getBytes());

			os.write(("--" + bd + "--\r\n").getBytes());
			os.flush();
			os.close();

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, "upload request http respose: " + con.getResponseCode() + " " + con.getResponseMessage());
				return null;
			}
			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.FINE, "IO exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Upload a file.
	 * @param data
	 * @return JSONObject if successful, null otherwise.
	 */
	private JSONObject gapi_insert(Data data) {		
		try {
			URL url = new URL("https://www.googleapis.com/upload/drive/v2/files?uploadType=media");
			HttpURLConnection con = gapi_connect(url, "POST", mAccessToken);
			con.setRequestProperty("Content-Type", data.getMimeType());
			con.setChunkedStreamingMode(8192);
			Utils.copyStream(data.getInputStream(), con.getOutputStream(), false);

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, "upload request http respose: " + con.getResponseCode() + " " + con.getResponseMessage());
				return null;
			}
			
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.FINE, "IO exception: " + e.getMessage());
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
			HttpURLConnection con = gapi_connect(url, "PUT", mAccessToken);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", String.valueOf(metajson.length()));
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(metajson);
			writer.flush();
			writer.close();

			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, "update failed: " + con.getResponseCode() + " " + con.getResponseMessage() + " " + Utils.readStream(con.getInputStream()));
				return null;
			}
			String json = Utils.readStream(con.getInputStream());
			return (JSONObject) JSONValue.parse(json);
		}
		catch (IOException e) {
			log.log(Level.FINE, "update failed: " + e.getMessage());
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
			HttpURLConnection con = gapi_connect(ub.toUrl(), "GET", mAccessToken);
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, "gapi_list_files http response: " + con.getResponseCode());
				log.log(Level.FINE, "response messaage: " + con.getResponseMessage());
				return null;
			}
			String json = Utils.readStream(con.getInputStream());
			JSONObject obj = (JSONObject) JSONValue.parse(json);
			String kind = (String) obj.get("kind");
			if (kind.equals("drive#fileList")) {
				return obj;
			} else {
				log.log(Level.FINE, "wrong kind of response received: " + kind);
				log.log(Level.FINE, "drive#fileList was expected");
				return null;
			}
		}
		catch (IOException e) {
			log.log(Level.FINE, "IO error: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Download a file.
	 * @param url
	 * @return InputStream if successful, null otherwise.
	 */
	private InputStream gapi_download(String url) {
		log.log(Level.FINE, "downloading from: " + url);
		try {
			HttpURLConnection con = gapi_connect(new URL(url), "GET", mAccessToken);
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				log.log(Level.FINE, String.format(
					"download failed: %d %s",
					con.getResponseCode(),
					con.getResponseMessage()));
				return null;
			}
			return con.getInputStream();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Share a file with someone else.
	 * @param fileid String file id.
	 * @param email Email of the account to share the file with.
	 * @return json response from the server.
	 */
	private JSONObject gapi_share_with(String fileid, String email) {
		try {
			JSONObject meta = new JSONObject();
			meta.put("type", "user");
			meta.put("value", email);
			meta.put("role", "reader");
			String metajson = meta.toJSONString();
			
			URL url = new URL("https://www.googleapis.com/drive/v2/files/" + fileid + "/permissions");
			HttpURLConnection con = gapi_connect(url, "POST", mAccessToken);
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
			log.log(Level.FINE, "exception: " + e.getMessage());
			return null;
		}
	}
}
