package org.lp20.aikuma.storage;

import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lp20.aikuma.storage.Utils.gapi_connect;
import static org.lp20.aikuma.storage.Utils.readStream;

/**
 * FusionIndex interfaces with the Aikuma Fusion Tables and the Aikuma Web Server
 * 
 *   - to search items in the Aikuma index, and
 *   - to make collected data public by registering it with the Aikuma index.
 * 
 * @author bob
 *
 */
public class FusionIndex implements Index {
    private static final int INSERT_STMT_LEN = 150; // a likely overestimated average size of an insert statement

    private String accessToken; // Google OAUTH access token
    private boolean catalogInitalized; // true if we've gotten the table name -> table ID mapping
    private final Map<String,String> catalog; // maps table names to FusionTables API table IDs

    private static final Set<String> requiredMetadata;
    static {
            requiredMetadata = new HashSet<String>(5);
            requiredMetadata.add("data_store_uri");
            requiredMetadata.add("item_id");
            requiredMetadata.add("file_type");
            requiredMetadata.add("language");
            requiredMetadata.add("speakers");
    }

    private static final Set<String> discourseTypes;
    static {
        discourseTypes = new HashSet<String>(10);
        discourseTypes.add("dialogue");
        discourseTypes.add("drama");
        discourseTypes.add("formulaic");
        discourseTypes.add("ludic");
        discourseTypes.add("oratory");
        discourseTypes.add("narrative");
        discourseTypes.add("procedural");
        discourseTypes.add("report");
        discourseTypes.add("singing");
        discourseTypes.add("unintelligible_speech");
    }

    private static final Set<String> multivalueKeys;
    static {
        multivalueKeys = new HashSet<String>(2);
        multivalueKeys.add("speakers");
        multivalueKeys.add("tags");
    }

    private static final Logger log = Logger.getLogger(FusionIndex.class.getName());


    public FusionIndex(String accessToken) {
        this.accessToken = accessToken;
        this.catalog = new HashMap<String, String>(1);
        this.catalogInitalized = false;
    }
	
	/* (non-Javadoc)
	 * @see org.lp20.aikuma.storage.Index#get_item_metadata(java.lang.String)
	 */
	@Override
	public Map<String,String> get_item_metadata(String identifier) {
        String data = getMetadata(identifier);
        if (data == null) return null;

        Map<String, String> ret = new HashMap<String, String>(10);
        Map json = (Map) JSONValue.parse(data);

        for (Object tmp: (Iterable) json.get("rows")) {
            List row = (List) tmp;
            String key = (String) row.get(2);
            String value = (String) row.get(3);
            if (multivalueKeys.contains(key) && ret.containsKey(key)) {
                ret.put(key, ret.get(key) + "," + value);
            } else {
                ret.put(key, value);
            }
        }
        return ret;
	}

    /**
     * Retrieve metadata from the FusionIndex API
     * @param forIdentifier the identifier for which to get data
     * @return an unparsed JSON string with the data; NB - if there's a problem getting
     */
    private String getMetadata(String forIdentifier) {
        if (!catalogInitalized) initializeCatalog();
        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?" +
                              "sql=SELECT+ROWID,+identifier,+key,+value+" +
                              "FROM+" + catalog.get("aikuma_metadata") + "+" +
                              "WHERE+identifier+=+'" + forIdentifier +"';");

            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return readStream(cn.getInputStream());
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new InvalidAccessTokenException();
            } else {
                log.warning("Identifier: " + forIdentifier);
                log.warning(String.valueOf(cn.getResponseCode()));
                log.warning(cn.getResponseMessage());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.lp20.aikuma.storage.Index#search(java.util.Map)
     */
	@Override
	public List<String> search(Map<String,String> constraints) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.lp20.aikuma.storage.Index#index(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void index(String identifier, Map<String,String> metadata) {
        validateMetadata(metadata);
        if (!catalogInitalized) initializeCatalog();
        if (!deleteMetadata(identifier)) {

        }
        String body = makeIndexSQL(identifier, metadata);

        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query");
            HttpURLConnection cn = gapi_connect(url, "POST", accessToken);
            OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
            out.write("sql=" + body);
            out.flush();
            out.close();
            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return;
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                throw new InvalidAccessTokenException();
            else {
                log.warning("Identifier: " + identifier);
                log.warning(String.valueOf(cn.getResponseCode()));
                log.warning(cn.getResponseMessage());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        log.warning("Error inserting metadata for " + identifier);
    }

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s WHERE ROWID ='%s';";
    private boolean deleteMetadata(String identifier) {
        String table = catalog.get("aikuma_metadata");
        Map json = (Map) JSONValue.parse(getMetadata(identifier));
        if (!json.containsKey("rows")) return true;
        for (Object row: (Iterable) json.get("rows")) {
            String rowid = (String)((List) row).get(0);
            try {
                HttpURLConnection cn = gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/query"),
                        "POST", accessToken);

                OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
                out.write("sql=" + String.format(DELETE_SQL_TEMPLATE, table, rowid));
                out.flush();
                out.close();
                if (cn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // success!
                }
                else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                    throw new InvalidAccessTokenException();
                else if (cn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST)
                    return false;
                else {
                    log.warning("Identifier: " + identifier);
                    log.warning(String.valueOf(cn.getResponseCode()));
                    log.warning(cn.getResponseMessage());
                    return false;
                }

            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        }
        return true;

    }

    private static final String INDEX_SQL_TEMPLATE = "INSERT INTO %s (identifier, key, value) VALUES ('%s', '%s', '%s');";
    private String makeIndexSQL(String identifier, Map<String, String> metadata) {
        String table = catalog.get("aikuma_metadata");
        Set<String> keys = metadata.keySet();
        StringBuilder sql = new StringBuilder(keys.size()  * INSERT_STMT_LEN);

        for (String key : keys) {
            // Building sql like this feels wrong; there's zero docs on how to handle escapes, etc.
            if (multivalueKeys.contains(key)) {
                String[] values = metadata.get(key).split(",");
                for (String value : values) {
                    sql.append(String.format(
                            INDEX_SQL_TEMPLATE,
                            table, identifier, key, value.replaceAll("'", "\'")
                    ));
                }
            } else {
                String value = metadata.get(key);
                sql.append(String.format(
                        INDEX_SQL_TEMPLATE,
                        table, identifier, key, value.replaceAll("'", "\'")
                ));
            }
        }
        try {
            return(URLEncoder.encode(sql.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Why the hell do I have to do this?
           return "";
        }

    }

    private void validateMetadata(Map<String, String> metadata) {
        for (String s: requiredMetadata) {
            if (!metadata.containsKey(s))
                throw new IllegalArgumentException("Missing required field " + s);
            if ("discourse_type".equals(s) && !discourseTypes.contains(metadata.get(s)))
                throw new IllegalArgumentException(s  + " is not a valid discourse_type");
        }
    }


    private void initializeCatalog() {
        // TODO implement this properly
        // GET and parse https://www.googleapis.com/fusiontables/v1/tables
        this.catalog.put("aikuma_metadata", "1MnBxX3Dv47iitW300X2n1kuQkFef3H4f0xsaQQm6");
        this.catalogInitalized = true;

    }



    /**
     * Get the list of OAUTH authentication scopes for the class
     * @return A list of authentication scope URLs for this class
     */
    public static Iterable<String> getScopes() {
        Collection<String> l = new ArrayList<String>(1);
        l.add("https://www.googleapis.com/auth/fusiontables");
        return l;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



}
