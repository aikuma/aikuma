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

    GoogleDriveFolderCache mCache;
    static boolean mCacheInitialized = false;

    String mAccessToken;
    String mRootId;
    String mCentralEmail;
    
    Api gapi;

    /**
     * GoogleDriveStorage allows saving and retrieving data to and from
     * Google Drive.
     *
     * Files are stored in a directory structure whose root folder is
     * identified by a root ID. The directory structure is initially read and
     * cached. Subsequent accesses to the directory structure is mediated by
     * the cache. Cache is static, therefore persistent throughout the life
     * time of the program. If the cache is invalidated, the cache can be
     * re-built by setting the {@code initialize} parameter to true.
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
     * @param initialize Tell whether to re-build the cache of the aikuma
     *        folder directory structure.
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

        mCache = GoogleDriveFolderCache.getInstance();
        if (initialize == true) {
            mCacheInitialized = false;
            mCache.clear();
        }
        if (mCacheInitialized == false) {
            initialize_aikuma_folder();
            mCacheInitialized = true;
        }

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
        String prefix = dirname(identifier);
        String pid = mCache.getFid(prefix);
        if (pid == null) {
            log.log(Level.FINE, "containing folder doesn't exist: " + prefix);
            return null;
        }

        String query = String.format(
                "trashed = false" +
                " and title = '%s'" +
                " and properties has {key='%s' and value='%s' and visibility='PUBLIC'}" +
                " and '%s' in parents",
                escape_quote(basename(identifier)),
                ROOT_FIELD, escape_quote(mRootId),
                pid);

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
        File f = new File(normpath(identifier));
        String parentId = mkdir(f.getParent());

        JSONObject meta = new JSONObject();
        meta.put("title", f.getName());

        JSONArray parents = new JSONArray();
        JSONObject parent = new JSONObject();
        parent.put("id", parentId);
        parents.add(parent);
        meta.put("parents", parents);
        meta.put("properties", getProp(f.getParent()));

        JSONObject obj = gapi.insertFile(data, meta);
        if (obj == null) return null;

        return (String) obj.get("id");
    }

    @Override
    public boolean share(String identifier) {
        String prefix = dirname(identifier);
        String pid = mCache.getFid(prefix);
        if (pid == null) {
            log.log(Level.FINE, "containing folder doesn't exist: " + prefix);
            return false;
        }

        String query = String.format(
                "trashed = false" +
                " and title = '%s'" +
                " and properties has {key='%s' and value='%s' and visibility='PUBLIC'}" +
                " and '%s' in parents",
                escape_quote(basename(identifier)),
                ROOT_FIELD, escape_quote(mRootId),
                pid);

        JSONObject obj = gapi.list(query, null);
        
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
        
        JSONObject r = gapi.shareWith(fileid, mCentralEmail);
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

        for (Search e = gapi.search(query); e.hasMoreElements();) {
            JSONObject o;
            try {
                o = e.nextElement();
            } catch (Search.Error err) {
                // TODO: need a way to pass exception to client
                log.log(Level.FINE, "search failed");
                return;
            }
            String identifier = "UNKNOWN";
            for (Object pidObj: (JSONArray) o.get("parents")) {
                JSONObject parent = (JSONObject) pidObj;
                String pid = (String) parent.get("id");
                String prefix = mCache.getPath(pid);
                if (prefix != null) {
                    identifier = joinpath(prefix, (String) o.get("title")).replaceAll("^/*","");
                    break;
                }
            }
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

    private Properties getProp(String prefix) {
        Properties props = new GoogleDriveStorage.Properties();
        props.put(DSVER_FIELD, DSVER);
        props.put(ROOT_FIELD, mRootId);
                props.put(PREFIX_FIELD, prefix);
        return props;
    }

    private void initialize_aikuma_folder() throws DataStore.StorageException {
        String query = String.format(
                "trashed = false" +
                " and 'me' in owners" +
                " and mimeType='%s'" +
                " and properties has {key='%s' and value='%s' and visibility='PUBLIC'}",
                FOLDER_MIME,
                ROOT_FIELD,
                escape_quote(mRootId));
        
        Search e = gapi.search(query);
        if (e.hasMoreElements()) {
            log.log(Level.FINE, "found aikumafied folders");
            mCache.beginTable();
            for (; e.hasMoreElements();) {
                JSONObject o;
                try {
                    o = e.nextElement();
                } catch (Search.Error err) {
                    log.log(Level.FINE, "search exception");
                                        try {
                        mCache.finishTable();
                    } catch (Exception err2) {
                        // ignore
                    }
                    throw new DataStore.StorageException();
                }
                String fid = (String) o.get("id");
                String title = (String) o.get("title");
                for (Object pidObj: (JSONArray) o.get("parents")) {
                    JSONObject parent = (JSONObject) pidObj;
                    mCache.addToTable(fid, title, (String) parent.get("id"));
                }
            }
            try {
                mCache.finishTable();
            } catch (GoogleDriveFolderCache.Error err) {
                throw new DataStore.StorageException();
            }
            if (mCache.getFid("/") == null) {
                log.log(Level.FINE, "no aikumafied root found -- falling back");
                mCache.clear();
                initialize_aikuma_folder2();
            }
        } else {
            log.log(Level.FINE, "didn't find aikumafied dir structure, falling back");
            initialize_aikuma_folder2();
        }
    }

    private void initialize_aikuma_folder2() throws DataStore.StorageException {
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
        meta.put("properties", getProp("/"));
        meta.put("title", "aikuma");
                meta.put("mimeType", FOLDER_MIME);
        JSONObject res = gapi.makeFile(meta);
                if (res == null)
                    throw new DataStore.StorageException();
        String fid = (String) res.get("id");
        mCache.add(fid, "/");
    }

    private void aikumafy(String fileid) throws DataStore.StorageException {
        log.log(Level.FINE, "aikumafying folder: " + fileid);
        Stack<String> stack = new Stack<String>();
        stack.push(fileid);
        stack.push("/");

        String q = "trashed = false and 'me' in owners and '%s' in parents and mimeType='" + FOLDER_MIME + "'";
        while (!stack.empty()) {
            String base = stack.pop();
            String pid = stack.pop();
            mCache.add(pid, base);
            aikumafy_update_properties(pid, base);
            for (Search e = gapi.search(String.format(q,pid)); e.hasMoreElements();) {
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

        q = "trashed=false and 'me' in owners and '%s' in parents and mimeType!='" + FOLDER_MIME + "'";
        for (String path:  mCache.listPaths()) {
            String pid = mCache.getFid(path);
            for (Search e = gapi.search(String.format(q,pid)); e.hasMoreElements();)
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
        meta.put("properties", getProp(dirname(path)));
                log.log(Level.FINE, "setting properties for " + fileid + ": " + meta.toString());
        gapi.updateMetadata(fileid, meta);
    }

    /**
     * Search files shared by other accounts, make a copy of the shared file,
     * and remove the shared file.
     */
    private void import_shared_files() {
        String query = String.format(
                "trashed = false" +
                " and sharedWithMe" +
                " and properties has {key='%s' and value='%s' and visibility='PUBLIC'}" +
                " and not ('%s' in parents)",
                DSVER_FIELD,
                DSVER,
                mkdir("/trash"));

        for (Search e = gapi.search(query); e.hasMoreElements();) {
            JSONObject obj;
            try {
                obj = e.nextElement();
            } catch (Search.Error err) {
                log.log(Level.FINE, "search error");
                break;
            }
            String fid = (String) obj.get("id");
            String prefix = props_to_map(obj.get("properties")).get(PREFIX_FIELD);
            if (prefix == null) {
                log.log(Level.FINE, "file has no prefix: " + fid);
                continue;
            }
            String pid = mkdir(prefix);
            JSONObject meta = new JSONObject();
            meta.put("properties", getProp(prefix));
            JSONArray parents = new JSONArray();
            JSONObject parent = new JSONObject();
            parent.put("id", pid);
            parents.add(parent);
            meta.put("parents", parents);
            if (gapi.copyFile(fid, meta) == null)
                log.log(Level.FINE, "failed to copy: " + fid);
            if (gapi_trash_file2(fid) == null)
                log.log(Level.FINE, "failed to trash file: " + fid);
        }
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
        String fid = mCache.getFid(f.getPath());
        if (fid == null) {
            String parentFid = mkdir(f.getParent());

            JSONObject meta = new JSONObject();
            meta.put("properties", getProp(f.getParent()));
            meta.put("title", f.getName());
            meta.put("mimeType", FOLDER_MIME);

            JSONArray parents = new JSONArray();
            JSONObject parent = new JSONObject();
            parent.put("id", parentFid);
            parents.add(parent);
            meta.put("parents", parents);

            JSONObject res = gapi.makeFile(meta);
            fid = (String) res.get("id");
            mCache.add(fid, f.getPath());
        }
        return fid;
    }

    /**
     * Trash given file into Aikuma trash dir.
     *
     * @param fileid Google file ID.
     * @return JSONObject on success, null otherwise.
     */
    private JSONObject gapi_trash_file2(String fileid) {
        String pid = mkdir("/trash");
        JSONObject meta = new JSONObject();
        JSONArray parents = new JSONArray();
        JSONObject parent = new JSONObject();
        parent.put("id", pid);
        parents.add(parent);
        meta.put("parents", parents);
        JSONObject res = gapi.updateMetadata(fileid, meta);
        if (res == null) {
            log.log(Level.FINE, "failed trash into aikuma/trash: " + fileid);
            return null;
        }
        return res;
    }
}
