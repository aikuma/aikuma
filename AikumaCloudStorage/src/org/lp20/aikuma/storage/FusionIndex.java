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
    private static final Logger log = Logger.getLogger(FusionIndex.class.getName());

    private static final class FieldInfo {
        boolean required;
        boolean multiValue;
        FieldInfo(boolean required, boolean multiValue) {
            this.required = required;
            this.multiValue = multiValue;
        }
    }


    private static final Map<String, FieldInfo> fields; // if true, required
    static {
        fields = new TreeMap<String, FieldInfo>();
        fields.put("data_store_uri", new FieldInfo(true, false));
        fields.put("item_id", new FieldInfo(true, false));
        fields.put("file_type", new FieldInfo(true, false));
        fields.put("language", new FieldInfo(true, false));
        fields.put("speakers", new FieldInfo(true, true));
        fields.put("tags", new FieldInfo(false, true));
        fields.put("discourse_types", new FieldInfo(false, true));
        fields.put("date_backedup", new FieldInfo(false, false));
        fields.put("date_approved", new FieldInfo(false, false));
    }

    private static final Set<String> discourseTypes;
    static {
        discourseTypes = new TreeSet<String>();
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


    private static final String INDEX_SQL_TEMPLATE = "INSERT INTO %s (identifier, %s) VALUES ('%s', %s)";
    private static final String SELECT_SQL_TEMPLATE;

    static {
        boolean header = false;
        String fieldList = "";
        for (String field : fields.keySet()) {
            if (header) fieldList += ", ";
            else header = true;
            fieldList += field;
        }
        SELECT_SQL_TEMPLATE = "SELECT " + fieldList + " FROM %s WHERE identifier = '%s';";
    }

    private static String urlencode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.log(Level.SEVERE, "Probable programming error: " + e.getMessage(), e);
            return "";
        }
    }

    private String accessToken; // Google OAUTH access token
    private boolean catalogInitalized; // true if we've gotten the table name -> table ID mapping
    private final Map<String,String> catalog; // maps table names to FusionTables API table IDs


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
        Map json = getMetadata(identifier);
        if (json == null || !json.containsKey("rows")) return null;
        Map<String, String> ret = new HashMap<String, String>(10);

        List<String> columns = (List<String>) json.get("columns");
        List<String> row = (List<String>) ((List) json.get("rows")).get(0);

        for (int i = 0; i < columns.size(); i++) {
            String key = columns.get(i);
            String value = row.get(i);
            if (fields.get(key).multiValue) {
                value = value.replace('|', ',').substring(1, value.length() - 1);
            }
            ret.put(key, value);
        }
        return ret;
	}
    /**
    * Get rowid for an identifier
    * @param forIdentifier the identifier for which to get data
    * @return a rowid
            */
    private String getRowId(String forIdentifier) {
        if (!catalogInitalized) initializeCatalog();
        try {
            String sql = urlencode(String.format("SELECT ROWID FROM %s WHERE identifier = '%s';", catalog.get("aikuma_metadata"), forIdentifier));
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?sql=" + sql);
            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Object tmp = JSONValue.parse(readStream(cn.getInputStream()));
                //TODO fix the ugly mess below
                return (String) ((List) ((List) ((Map) tmp).get("rows")).get(0)).get(0);
            }
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                throw new InvalidAccessTokenException();
            else {
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
    /**
     * Retrieve metadata from the FusionIndex API
     * @param forIdentifier the identifier for which to get data
     * @return an unparsed JSON string with the data; NB - if there's a problem getting data, returns null
     */
    private Map getMetadata(String forIdentifier) {
        if (!catalogInitalized) initializeCatalog();
        try {
            String sql = urlencode(String.format(SELECT_SQL_TEMPLATE, catalog.get("aikuma_metadata"), forIdentifier));
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?sql=" + sql);
            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return (Map) JSONValue.parse(readStream(cn.getInputStream()));
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                throw new InvalidAccessTokenException();
             else {
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
        if (getRowId(identifier) != null) {
            log.severe("index called on item with existing index entry");
            return;
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

    @Override
    public void update(String identifier, Map<String, String> metadata) {
        throw new RuntimeException("Not implemented");
    }


    private String makeIndexSQL(String identifier, Map<String, String> metadata) {
        String table = catalog.get("aikuma_metadata");
        StringBuilder fieldList = new StringBuilder();
        StringBuilder valueList = new StringBuilder();

        boolean header = false;
        for (Map.Entry<String, String> e : metadata.entrySet()) {
            if (header) {
                fieldList.append(", ");
                valueList.append(", ");
            }  else header = true;
            String key = e.getKey();
            fieldList.append((key));
            String value = e.getValue().replaceAll("'", "\'");
            if (fields.get(key).multiValue) value = "|" + value.replaceAll("\\s*,\\s*", "|") + "|";
            valueList.append("'"+ value + "'");
        }
        return urlencode(String.format(INDEX_SQL_TEMPLATE, table, fieldList.toString(), identifier,
                valueList.toString()));
    }

    private void validateMetadata(Map<String, String> metadata) {
        for (Map.Entry<String, FieldInfo> info: fields.entrySet()) {
            String key = info.getKey();
            FieldInfo value =  info.getValue();
            if (value.required && !metadata.containsKey(key))
                throw new IllegalArgumentException("Missing required field " + key);
            if ("discourse_type".equals(key) && !discourseTypes.contains(metadata.get(key)))
                throw new IllegalArgumentException(metadata.get(key)  + " is not a valid discourse_type");
        }
    }


    private void initializeCatalog() {
        // TODO implement this properly
        // GET and parse https://www.googleapis.com/fusiontables/v1/tables
        this.catalog.put("aikuma_metadata", "1Kw1vNV3BpSlInhZeSh5l36__Qsnz1JvwSXuJgfhD");
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
