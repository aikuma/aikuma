package org.lp20.aikuma.storage.google;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.*;

import org.lp20.aikuma.net.*;
import org.lp20.aikuma.storage.*;

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
    static final String ROOT_FILE = "aikuma_root_id.txt";
    static final String PREFIX_FIELD = "aikuma_prefix";
    static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    String mAccessToken;
    String mRootId;
    String mRootFileId;
    String mCentralEmail;
    
    Api gapi;

    /**
     * GoogleDriveStorage allows saving and retrieving data to and from
     * Google Drive.
     *
     * Files are stored in a directory structure whose root folder is
     * identified by a root ID.
     *
     * Clients can share files with another google account. The email address
     * of the account should be provided by the centralEmail parameter. The
     * {@code share()} method uses this information to share files with that
     * account.
     *
     * To access a google drive account, it requires an access token for the
     * scopes specified by the {@code getScopes()} method. The access token
     * can be obtained in different ways. The following example uses the
     * {@code GoogleAuth} class.
     * 
     * {@code
     * GoogleAuth auth = new GoogleAuth(myClientId, myClientSecret);
     * String authCode = get_auth_code_for_scopes(GoogleDriveStorage.getScopes());
     * auth.requestAccessToken(authCode);
     * String accessToken = auth.getAccessToken();
     * }
     * 
     * @param accessToken Access token for a google drive account.
     * @param rootId Globally unique identifier for the root directory.
     * @param centeralEmail Email address to share stored files with.
     * @param initialize Ignored. Kept for backward compatibility.
     */
    public GoogleDriveStorage(
            String accessToken,
            String rootId,
            String centralEmail,
            boolean initialize)
        throws DataStore.StorageException
    {
        mAccessToken = accessToken;
        mRootId = rootId;
        mCentralEmail = centralEmail;

        gapi = new Api(new TokenManager() {
            public String accessToken() {
                return mAccessToken;
            }
        });

        mRootFileId = initialize_aikuma_folder();

        import_shared_files();
    }
    
    public GoogleDriveStorage(
            String accessToken,
            String rootId,
            String centralEmail)
        throws DataStore.StorageException
    {
        this(accessToken, rootId, centralEmail, false);
    }

    @Override
    public InputStream load(String identifier) {
        String query = String.format(
                "trashed = false" +
                " and title = '%s'" +
                " and '%s' in parents" +
                " and mimeType != '%s'",
                escape_quote(identifier), mRootFileId, FOLDER_MIME);

        JSONObject obj = gapi.list(query, null);

        if (obj == null) return null;
        JSONArray arr = (JSONArray) obj.get("items");
        if (arr.size() == 0)
            return null;
        JSONObject item = (JSONObject) arr.get(0);
        String url = (String) item.get("downloadUrl");
        return gapi.download(url);
    }

    @Override
    public String store(String identifier, Data data) {
        JSONObject meta = new JSONObject();
        meta.put("title", identifier);

        JSONArray parents = new JSONArray();
        JSONObject parent = new JSONObject();
        parent.put("id", mRootFileId);
        parents.add(parent);
        meta.put("parents", parents);

        JSONObject obj = gapi.insertFile(data, meta);
        if (obj == null) return null;

        return (String) obj.get("id");
    }

    @Override
    public boolean share(String identifier) {
        String query = String.format(
                "trashed = false" +
                " and title = '%s'" +
                " and '%s' in parents" + 
                " and mimeType != '%s'",
                escape_quote(identifier), mRootFileId, FOLDER_MIME);

        JSONObject obj = gapi.list(query, null);
        
        if (obj == null) return false;
        
        String fileid = null;
        JSONArray arr = (JSONArray) obj.get("items");
        if (arr != null && arr.size() > 0) {
            JSONObject o = (JSONObject) arr.get(0);
            fileid = (String) o.get("id");
        } else {
            log.log(Level.FINE, "no file by identifier: " + identifier);
            return false;
        }
        
        JSONObject r = gapi.shareWith(fileid, mCentralEmail);
        return r != null;
    }
    
    @Override
    public void list(ListItemHandler listItemHandler) {
        String query = String.format(
                "trashed = false" +
                " and mimeType != '%s'" +
                " and '%s' in parents",
                FOLDER_MIME, mRootFileId);

        for (Search e = gapi.search(query); e.hasMoreElements();) {
            JSONObject o;
            try {
                o = e.nextElement();
            } catch (Search.Error err) {
                // TODO: need a way to pass exception to client
                log.log(Level.FINE, "search failed");
                return;
            }
            String identifier = (String) o.get("title");
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

    private Properties getProp(String prefix) {
        Properties props = new GoogleDriveStorage.Properties();
        props.put(DSVER_FIELD, DSVER);
        props.put(ROOT_FIELD, mRootId);
        props.put(PREFIX_FIELD, prefix);
        return props;
    }

    private String initialize_aikuma_folder() throws DataStore.StorageException {
        String query = String.format(
                "trashed = false" +
                " and 'root' in parents" +
                " and 'me' in owners" +
                " and mimeType='%s'" +
                " and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
                FOLDER_MIME,
                ROOT_FIELD, escape_quote(mRootId));
        
        Search e = gapi.search(query);
        if (e.hasMoreElements()) {
            String fid = null;
            try {
                fid = (String) ((JSONObject) e.nextElement()).get("id");
                log.log(Level.FINE, "found aikumafied folder");
            } catch (Search.Error err) {
                log.log(Level.FINE, "failed to find aikumafied folder");
                throw new DataStore.StorageException();
            }
            if (e.hasMoreElements()) {
                log.log(Level.FINE, "found more aikumafied folders");
                // more than one folder with the same root id
                throw new DataStore.StorageException();
            }
            return fid;
        } else {
            log.log(Level.FINE, "didn't find aikumafied folder, falling back");
            return initialize_aikuma_folder2();
        }
    }

    private String initialize_aikuma_folder2() throws DataStore.StorageException {
        String query = String.format(
                "trashed = false " +
                " and 'me' in owners" +
                " and title = '%s'" +
                " and mimeType != '%s'",
                ROOT_FILE,
                FOLDER_MIME);

        Search e = gapi.search(query);
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
                InputStream is = gapi.download((String) o.get("downloadUrl"));
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
                log.log(Level.FINE, "downloaded root id: " + id + " vs " + mRootId);
                if (id.equals(mRootId)) {
                    for (Object p: (JSONArray) o.get("parents")) {
                        String pid = (String) ((JSONObject) p).get("id");
                        parentIds.add(pid);
                    }
                }
            }
            if (parentIds.size() == 0) {
                log.log(Level.FINE, "failed to identify root folder containing the root id file");
                return initialize_aikuma_folder3();
            } else if (parentIds.size() == 1) {
                log.log(Level.FINE, "found manually created aikuma folder; setting root id");
                String fid = parentIds.get(0);
                JSONObject meta = new JSONObject();
                meta.put("properties", getProp("/"));
                if (gapi.updateMetadata(fid, meta) == null)
                    throw new DataStore.StorageException();
                return fid;
            } else {
                log.log(Level.FINE, "multiple Aikuma folders matching root id: " + mRootId);
                throw new DataStore.StorageException();
            }
        } else {
            return initialize_aikuma_folder3();
        }
    }    

    private String initialize_aikuma_folder3() throws DataStore.StorageException {
        log.log(Level.FINE, "creating a new root");
        JSONObject meta = new JSONObject();
        meta.put("properties", getProp("/"));
        meta.put("title", "aikuma");
        meta.put("mimeType", FOLDER_MIME);
        JSONObject res = gapi.makeFile(meta);
        if (res == null)
            throw new DataStore.StorageException();
        return (String) res.get("id");
    }

    /**
     * Search files shared by other accounts, make a copy of the shared file,
     * and remove the shared file.
     */
    private void import_shared_files() {
        String query = String.format(
                "trashed = false" +
                " and sharedWithMe" +
                " and title contains 'aikuma/%s'" +
                " and not ('%s' in parents)",
                DSVER, mRootFileId);

        JSONObject meta = new JSONObject();
        JSONArray parents = new JSONArray();
        JSONObject parent = new JSONObject();
        parent.put("id", mRootFileId);
        parents.add(parent);
        meta.put("parents", parents);

        for (Search e = gapi.search(query); e.hasMoreElements();) {
            JSONObject obj;
            try {
                obj = e.nextElement();
            } catch (Search.Error err) {
                log.log(Level.FINE, "search error");
                break;
            }
            String fid = (String) obj.get("id");
            if (gapi.updateMetadata(fid, meta) == null)
                log.log(Level.FINE, "failed to move to aikuma folder: " + fid);
        }
    }
}
