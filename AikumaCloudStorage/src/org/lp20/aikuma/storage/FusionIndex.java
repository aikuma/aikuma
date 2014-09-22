package org.lp20.aikuma.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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


    public static enum MetadataField {
        DATA_STORE_URI("data_store_uri", true, false),
        ITEM_ID("item_id", true, false),
        FILE_TYPE("file_type", true, false),
        LANGUAGES("languages", true, true),
        SPEAKERS("speakers", true, true),
        TAGS("tags", false, true),
        DISCOURSE_TYPES("discourse_types", false, true),
        DATE_BACKED_UP("date_backedup", false, false, "yyyy-mm-dd'T'hh:mm:ssZ"),
        DATE_APPROVED("date_approved", false, false, "yyyy-mm-dd'T'hh:mm:ssZ"),
        METADATA("metadata", false, false),
        USER_ID("user_id", true, false),
        DATE_PROCESSED("date_processed", false, false, "yyyy-mm-dd'T'hh:mm:ssZ");

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isMultivalue() {
            return multivalue;
        }

        public String getFormat() {
            return format;
        }

        private String name;
        private boolean required;
        private boolean multivalue;
        private String format;

        private MetadataField(String name, boolean required, boolean multivalue, String format) {
            this.name = name;
            this.required = required;
            this.multivalue = multivalue;
            this.format = format;
        }
        private MetadataField(String name, boolean required, boolean multivalue) {
            this(name, required, multivalue, null);
        }
        private static Map<String, MetadataField> nameToValue;
        static  {
            nameToValue = new HashMap<String, MetadataField>(values().length);
            for (MetadataField f : values()) {
                nameToValue.put(f.name, f);
            }
        }
        public static MetadataField byName(String name) {
            return nameToValue.get(name);
        }
        public static boolean isValidName(String fieldName) {
            return nameToValue.containsKey(fieldName);
        }

        public static boolean areValidNames(Iterable<String> fieldNames) {
            int count = 0;
            Set<String> keys = nameToValue.keySet();
            for (String name : fieldNames) {
                if (!keys.contains(name)) return false;
                count++;
            }
            if (count > 0)
                return true;
            else
                return false;
        }

    }

    public static enum DiscourseType {
        DIALOGUE("dialogue"),
        DRAMA("drama"),
        FORMULAIC("formulaic"),
        LUDIC("ludic"),
        ORATORY("oratory"),
        NARRATIVE("narrative"),
        PROCEDURAL("procedural"),
        REPORT("report"),
        SINGING("singing"),
        UNINTELLIGIBLE_SPEECH("unintelligible_speech");

        private String name;
        public String getName() {
            return name;
        }


        private DiscourseType(String name) {
            this.name = name;
        }

        private static Map<String, DiscourseType> nameToValue;
        static {
            for (DiscourseType d: values()) {
                nameToValue.put(d.getName(), d);
            }

        }
        public static DiscourseType byName(String name) {
            return nameToValue.get(name);
        }

        public static boolean isValidName(String name) {
            return nameToValue.containsKey(name);
        }
    }


    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s (identifier, %s) VALUES ('%s', %s)";
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE %s SET %s WHERE ROWID = '%s';";
    private static final String SELECT_SQL_TEMPLATE;
    //TODO decide, should I just make this select *?
    static {
        boolean header = false;
        String fieldList = "";
        for (MetadataField field : MetadataField.values()) {
            if (header) fieldList += ", ";
            else header = true;
            fieldList += field.getName();
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
    @SuppressWarnings("unchecked")
	public Map<String,String> getItemMetadata(String identifier) {
        Map json = getMetadata(identifier);
        if (json == null || !json.containsKey("rows")) return null;
        Map<String, String> ret = new HashMap<String, String>(10);
        List<String> columns = (List<String>) json.get("columns");
        List<String> row = (List<String>) ((List) json.get("rows")).get(0);

        for (int i = 0; i < columns.size(); i++) {
            String key = columns.get(i);
            String value = row.get(i);
            if (MetadataField.byName(key).isMultivalue() && value.length() > 0) {
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
        if (tmp != null) {
        	JSONArray arr = (JSONArray) ((JSONObject) tmp).get("rows");
        	if (arr != null && arr.size() > 0) {
        		return (String) ((JSONArray) arr.get(0)).get(0);
        	}
        }
        return null;
    }

    private Object doGet(String forIdentifier, String sql) {
        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query?sql=" + urlencode(sql));
            HttpURLConnection cn = gapi_connect(url, "GET", accessToken);

            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return JSONValue.parse(readStream(cn.getInputStream()));
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                log.warning("Invalid access token: " + this.accessToken);
                throw new InvalidAccessTokenException();
            } else {
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

    /**
     * @see org.lp20.aikuma.storage.Index#search(java.util.Map)
     */
	@Override
	public List<String> search(Map<String,String> constraints) {
        List<String> retval = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();

        sql.append(String.format("SELECT identifier FROM %s WHERE date_approved NOT EQUAL TO ''", tableId));
        for (String key: constraints.keySet()) {
            if (!MetadataField.isValidName(key))
                throw new IllegalArgumentException("Unknown field: " + key);
            sql.append(" AND ");
            if (MetadataField.byName(key).isMultivalue())
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

    /**
     * Validates search parameters
     * @param constraints
     */
    private void validateConstraints(Map<String, String> constraints) {
        for (String key : constraints.keySet()) {
            if (!MetadataField.isValidName(key))
                throw new IllegalArgumentException("Unknown key " + key);
        }
    }

    /* (non-Javadoc)
     * @see org.lp20.aikuma.storage.Index#index(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
	@Override
	public boolean index(String identifier, Map<String,String> metadata) {
        validateMetadata(metadata, true);
        if (getRowId(identifier) != null) {
            log.severe("index called on item with existing index entry");
            return false;
        }
        return doPost(identifier, makeInsert(identifier, metadata));
    }

    @Override
    public boolean update(String identifier, Map<String, String> metadata) {
        validateMetadata(metadata, false);
        String rowid = getRowId(identifier);
        if (rowid == null) {
            log.severe("update called on item without an existing index entry");
            return false;
        }
        doPost(identifier, makeUpdate(rowid, metadata));
        return true;
    }

    private boolean doPost(String identifier, String body) {
        try {
            URL url = new URL("https://www.googleapis.com/fusiontables/v1/query");
            HttpURLConnection cn = gapi_connect(url, "POST", accessToken);
            OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
            out.write("sql=" + body);
            out.flush();
            out.close();
            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return true;
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
        return false;
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
            if (MetadataField.byName(key).isMultivalue()) {
                value = "|" + value.replaceAll("\\s*,\\s*", "|") + "|";
            }
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
            if (MetadataField.byName(field).isMultivalue()) {
                value = "|" + value.replaceAll("\\s*,\\s*", "|") + "|";
            }
            sql.append(field).append(" = '").append(value).append("'");
        }
        return urlencode(String.format(UPDATE_SQL_TEMPLATE, tableId, sql.toString(), rowid));
    }
    private void validateMetadata(Map<String, String> metadata, boolean isInsert) {
        if (!MetadataField.areValidNames(metadata.keySet())) {
            throw new IllegalArgumentException("Unknown metadata keys");
        }
        for (MetadataField f: MetadataField.values()) {
            String name = f.getName();
            if (isInsert && f.isRequired( )&& !metadata.containsKey(name))
                throw new IllegalArgumentException("Missing required field " + name);
            if (f.getFormat() != null) {
                // TODO: this assumes all formats are date formats; it should probably be different
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(f.getFormat());
                    Date tmp = sdf.parse(metadata.get(name));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid data format for " + name +
                                                       "; does not match" + f.getFormat());
                }
            }
            if ("discourse_type".equals(name)) {
                for (String tmp : metadata.get(name).split(",")) {
                    if (!DiscourseType.isValidName(tmp))
                        throw new IllegalArgumentException(metadata.get(name) + " is not a valid discourse_type");
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
        l.add("https://www.googleapis.com/auth/drive.metadata.readonly");
        return l;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



}
