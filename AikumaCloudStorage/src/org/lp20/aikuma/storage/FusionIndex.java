package org.lp20.aikuma.storage;

import org.json.simple.JSONObject;
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
    private String tableId;

    private static final class FieldInfo {
        final boolean required;
        final boolean multiValue;
        FieldInfo(boolean required, boolean multiValue) {
            this.required = required;
            this.multiValue = multiValue;
        }
    }


    private static final Map<String, FieldInfo> fields;
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


    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s (identifier, %s) VALUES ('%s', %s)";
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE %s SET %s WHERE ROWID = '%s';";
    private static final String SELECT_SQL_TEMPLATE;
    //TODO decide, should I just make this select *?
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


    public FusionIndex(String accessToken) {
        this.accessToken = accessToken;
        this.tableId = "1Kw1vNV3BpSlInhZeSh5l36__Qsnz1JvwSXuJgfhD";

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
        String sql = String.format("SELECT ROWID FROM %s WHERE identifier = '%s';", tableId, forIdentifier);
        Object tmp = doGet(forIdentifier, sql);
        //TODO fix the ugly mess below
        if (tmp != null)
        return (String) ((List) ((List) ((Map) tmp).get("rows")).get(0)).get(0);
        else return null;
    }

    private Object doGet(String forIdentifier, String sql) {
        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?sql=" + urlencode(sql));
            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return JSONValue.parse(readStream(cn.getInputStream()));
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                throw new InvalidAccessTokenException();
            else {
                log.warning("Identifier: " + forIdentifier);
                log.warning(String.valueOf(cn.getResponseCode()));
                log.warning(cn.getResponseMessage());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retrieve metadata from the FusionIndex API
     * @param forIdentifier the identifier for which to get data
     * @return an unparsed JSON string with the data; NB - if there's a problem getting data, returns null
     */
    private Map getMetadata(String forIdentifier) {
        String sql = String.format(SELECT_SQL_TEMPLATE, tableId, forIdentifier);
        Object tmp = doGet(forIdentifier, sql);
        if (tmp != null)
            return (Map) tmp;
        else
            return null;

    }

    /* (non-Javadoc)
     * @see org.lp20.aikuma.storage.Index#search(java.util.Map)
     */
	@Override
	public List<String> search(Map<String,String> constraints) {
        List<String> retval = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("SELECT identifier FROM %s WHERE date_approved NOT EQUAL TO ''", tableId));
        for (String key: constraints.keySet()) {
            if (!fields.containsKey(key))
                throw new IllegalArgumentException("Unknown field: " + key);
            sql.append(" AND ");
            if (fields.get(key).multiValue)
                sql.append(String.format("%s CONTAINS '|%s|'", key, constraints.get(key)));
            else
                sql.append(String.format("%s = '%s'", key, constraints.get(key)));
        }
        sql.append(";");
        JSONObject tmp = (JSONObject) doGet("[None]", sql.toString());
        if (tmp.containsKey("rows"))  {
            for (Object row : (List) tmp.get("rows")) {
                retval.add((String) ((List) row).get(0));
            }

        }
        return retval;
	}

    private void validateConstraints(Map<String, String> constraints) {
        for (String key : constraints.keySet()) {
            if (!fields.containsKey(key))
                throw new IllegalArgumentException("Unknown key " + key);
        }
    }

    /* (non-Javadoc)
     * @see org.lp20.aikuma.storage.Index#index(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
	@Override
	public void index(String identifier, Map<String,String> metadata) {
        validateMetadata(metadata, true);
        if (getRowId(identifier) != null) {
            log.severe("index called on item with existing index entry");
            return;
        }
        doPost(identifier, makeInsert(identifier, metadata));
    }

    @Override
    public void update(String identifier, Map<String, String> metadata) {
        validateMetadata(metadata, false);
        String rowid = getRowId(identifier);
        if (rowid == null) {
            log.severe("update called on item without an existing index entry");
            return;
        }
        doPost(identifier, makeUpdate(rowid, metadata));
    }

    private void doPost(String identifier, String body) {
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
                log.warning(url.toString());
                log.warning(String.valueOf(cn.getResponseCode()));
                log.warning(cn.getResponseMessage());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        log.warning("Error inserting metadata for " + identifier);
    }


    private String makeInsert(String identifier, Map<String, String> metadata) {
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
        return urlencode(String.format(INSERT_SQL_TEMPLATE, tableId, fieldList.toString(), identifier,
                valueList.toString()));
    }
    private String makeUpdate(String rowid, Map<String, String> metadata) {
        StringBuilder sql = new StringBuilder();

        boolean header = false;
        for (Map.Entry<String, String> e : metadata.entrySet()) {
            if (header)  sql.append(", ");
            else header = true;
            String field = e.getKey();
            String value = e.getValue().replaceAll("'", "\'");
            if (fields.get(field).multiValue) value = "|" + value.replaceAll("\\s*,\\s*", "|") + "|";
            sql.append(field).append(" = '").append(value).append("'");
        }
        return urlencode(String.format(UPDATE_SQL_TEMPLATE, tableId, sql.toString(), rowid));
    }
    private void validateMetadata(Map<String, String> metadata, boolean isInsert) {
        if (!fields.keySet().containsAll(metadata.keySet()))
            throw new IllegalArgumentException("Unknown metadata keys");
        for (Map.Entry<String, FieldInfo> info: fields.entrySet()) {
            String key = info.getKey();
            FieldInfo value = info.getValue();
            if (isInsert && value.required && !metadata.containsKey(key))
                throw new IllegalArgumentException("Missing required field " + key);
            if ("discourse_type".equals(key)) {
                for (String tmp : metadata.get(key).split(",")) {
                    if (!discourseTypes.contains(tmp))
                        throw new IllegalArgumentException(metadata.get(key) + " is not a valid discourse_type");
                }
            }
        }
    }

    /**
     * Set the FusionTable ID for the Aikuma metadata table
     * @param tableId the ID
     */
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    /**
     * Get the FusionTable ID for the Aikuma metadata table
     * @return the ID
     */
    public String getTableId() {
        return this.tableId;
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
