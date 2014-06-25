package org.lp20.aikuma.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static org.lp20.aikuma.storage.Utils.gapi_connect;
import static org.lp20.aikuma.storage.Utils.readStream;

/**
 * FusionIndex interfaces with the Aikuma Fusion Tables and the Aikuma Web Server
 * 
 *   - to search items in the Aikuma index, and
 *   - to make collected data public by registering it with the Aikuma index.
 * 
 * @author haejoong
 *
 */
public class FusionIndex implements Index {
    private static final int INSERT_STMT_LEN = 150; // a likely overestimated average size of an insert statement

    private String accessToken; // Google OAUTH access token
    private boolean catalogInitalized; // true if we've gotten the table name -> table ID mapping
    private final Map<String,String> catalog; // maps table names to FusionTables API table IDs

    public static final Set<String> requiredMetadata;
    static {
            requiredMetadata = new HashSet<String>(5);
            requiredMetadata.add("data_store_uri");
            requiredMetadata.add("item_id");
            requiredMetadata.add("file_type");
            requiredMetadata.add("language");
            requiredMetadata.add("speakers");
    };

    public static final Set<String> discourseTypes;
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
        //multivalueKeys.add("speakers");
        //multivalueKeys.add("tags");
        multivalueKeys.add("people");
        multivalueKeys.add("languages");
    }



    public FusionIndex(String accessToken) {
        this.accessToken = accessToken;
        this.catalog = new HashMap<String, String>(1);
        this.catalogInitalized = false;
    }
	
	/* (non-Javadoc)
	 * @see org.lp20.aikma.storage.Index#get_item_metadata(java.lang.String)
	 */
	@Override
	public Map<String,String> get_item_metadata(String identifier) {
        String data = getMetadata(identifier);
        if (data == null) return null;

        HashMap<String, String> ret = new HashMap<String, String>(10);
        JSONObject json = (JSONObject) JSONValue.parse(data);

        for (Object tmp: (JSONArray) json.get("rows")) {
            JSONArray row = (JSONArray) tmp;
            String key = (String) row.get(2);
            String value = (String) row.get(3);
            if (multivalueKeys.contains(key) && ret.containsKey(key)) {
                ret.put(key, ((String) ret.get(key)) + "," + value);
            } else {
                ret.put(key, value);
            }
        }
        return ret;
	}

    private String getMetadata(String forIdentifier) {
        if (!catalogInitalized) initializeCatalog();
        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?" +
                              "sql=SELECT+ROWID,+identifier,+key,+value+" +
                              "FROM+" + (String) catalog.get("aikuma_metadata") + "+" +
                               "WHERE+identifier+=+'" + forIdentifier +"';");

            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == cn.HTTP_OK)
                return readStream(cn.getInputStream());
            else if (cn.getResponseCode() == cn.HTTP_UNAUTHORIZED) {
                // TODO review fusiontables API docs and make sure this is the only reason we'd get a 401
                throw new InvalidAccessTokenException();
            } else {
                // TODO replace this with appropriate logging
                System.out.println(cn.getResponseCode());
                System.out.println(cn.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Unable to get metadata");
    }

    /* (non-Javadoc)
     * @see org.lp20.aikma.storage.Index#search(java.util.Map)
     */
	@Override
	public List<String> search(Map<String,String> constraints) {
		List<String> res = new ArrayList<String>();
		return res;
	}
	
	/* (non-Javadoc)
	 * @see org.lp20.aikma.storage.Index#index(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public boolean index(String identifier, Map<String,Object> metadata) {
		// Send the metadata to the server using the Aikuma Web API.

//        validateMetadata(metadata);
        if (!catalogInitalized) initializeCatalog();
//        deleteMetadata(identifier); // easier to delete than try to update
        String body = makeIndexSQL(identifier, metadata);

        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query");
            HttpURLConnection cn = gapi_connect(url, "POST", accessToken);
            OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
            out.write("sql=" + body);
            out.flush();
            out.close();
            if (cn.getResponseCode() == cn.HTTP_OK)
                return true;
            else if (cn.getResponseCode() == cn.HTTP_UNAUTHORIZED)
                throw new InvalidAccessTokenException();
            else {
            	Log.i("hi", body);
      
                System.out.println(cn.getResponseCode());
                System.out.println(cn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s WHERE ROWID ='%s';";
    private boolean deleteMetadata(String identifier) {
        String table =  (String) catalog.get("aikuma_metadata");
        JSONObject json = (JSONObject) JSONValue.parse(getMetadata(identifier));
        if (!((Map)json).containsKey("rows")) return true;
        for (Object row: (List) ((Map) json).get("rows")) {
            String rowid = (String)((List) row).get(0);
            try {
                HttpURLConnection cn = gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/query"),
                        "POST", accessToken);

                OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
                out.write("sql=" + String.format(DELETE_SQL_TEMPLATE, table, rowid));
                out.flush();
                out.close();
                if (cn.getResponseCode() == cn.HTTP_OK)
                    continue;
                else if (cn.getResponseCode() == cn.HTTP_UNAUTHORIZED)
                    throw new InvalidAccessTokenException();
                else if (cn.getResponseCode() == cn.HTTP_BAD_REQUEST)
                    return false;
                else {
                    System.err.println("Unable to delete metadata for: " + identifier);
                    System.err.println("Got: " + cn.getResponseCode() + "(" +  cn.getResponseMessage()+ ")");
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;

    }

    private static final String INDEX_SQL_TEMPLATE = "INSERT INTO %s (identifier, key, value) VALUES ('%s', '%s', '%s');";
    private String makeIndexSQL(String identifier, Map<String, Object> metadata) {
        String table =  (String) catalog.get("aikuma_metadata");
        Set<String> keys = metadata.keySet();
        StringBuilder sql = new StringBuilder(keys.size()  * INSERT_STMT_LEN);

        // TODO I should be checking for existing data before I insert, so we don't wind up duplicate keys
        for (String key : keys) {
            // Building sql like this feels wrong; there's zero docs on how to handle escapes, etc.
            // RESOLVE should I be using imports here, since we're always doing multiple rows?
            if (multivalueKeys.contains(key)) {
//                String[] values = (metadata.get(key).toString()).split(",");
//                // seriously, would it kill them to make arrays support foreach?
//                for (int i = 0; i < values.length; i++) {
//                    String value = values[i];
//                    sql.append(String.format(
//                            INDEX_SQL_TEMPLATE,
//                            table, identifier, key, value.replaceAll("'", "\'")
//                    ));
//                }
            } else {
            	Object val = metadata.get(key);
            	if(val == null) break;
            	Log.i("hi", metadata.get(key).toString());
                String value = (String) metadata.get(key).toString();
                sql.append(String.format(
                        INDEX_SQL_TEMPLATE,
                        table, identifier, key, value.replaceAll("'", "\'")
                ));
            }
        }
        try {
            return(URLEncoder.encode(sql.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Why the hell do I have to do this? Checked exceptions sound like a
            // great idea till you actually use them.
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

    /**
     * Creates the FusionTables data structures for the FusionIndex
     *
     * @return true if successfully created, false otherwise.
     */
    public boolean createFusionIndex() {
        // TODO implement this
        // RESOLVE should this be in the normal index class, or a separate util class?


        return false;
    }

    private void initializeCatalog() {
        // TODO implement this properly
        // GET and parse https://www.googleapis.com/fusiontables/v1/tables
        this.catalog.put("aikuma_metadata", "1tn6_EGu1SWPIshGkbYX-bg8QEen9H98UJBkrObAu");
        this.catalogInitalized = true;

    }



    /**
     * Get the list of OAUTH authentication scopes for the class
     * @return A list of authentication scope URLs for this class
     */
    public static List<String> getScopes() {
        //TODO we should really extract the auth methods to a separate interface
        ArrayList<String> l = new ArrayList<String>(1);
        l.add("https://www.googleapis.com/auth/fusiontables");
        return l;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



}
