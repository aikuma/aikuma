import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.lp20.aikuma.storage.Utils.readStream;

/**
 * Created by bob on 6/4/14.
 *
 * A demo of adding a new FusionIndex item
 */
public class IndexTool {

    String tableId;
    String accessToken;

    Map<String,String> metadata;
    String identifier;



    public static void usage() {
        System.err.println("Usage: IndexTool <action> [<identifier>] [data.json]");

    }
    public static void main(String[] args) {
        String accessToken = null;
        String refreshToken = null;
        String tableId = null;
        String action = null;

        String client_id;
        String client_secret;

        try {
            Properties config = new Properties();
            config.load(new FileInputStream(new File(System.getProperty("user.home") + "/.aikuma")));
            accessToken = config.getProperty("access_token");
            refreshToken = config.getProperty("refresh_token");
            tableId = config.getProperty("table_id");
            client_id = config.getProperty("client_id");
            client_secret = config.getProperty("client_secret");

            GoogleAuth auth = new GoogleAuth(client_id, client_secret);

            if (!auth.validateAccessToken(accessToken)) {
                auth.refreshAccessToken(refreshToken);
                accessToken = auth.getAccessToken();
                //config.setProperty("access_token", accessToken);
                config.put("access_token", accessToken);
                config.store(new FileOutputStream(new File(System.getProperty("user.home") + "/.aikuma")), "Automatically updated with new access_token");
            }

        } catch (FileNotFoundException e) {
            System.err.println("No .aikuma prefs file found in your home directory.\n" +
                               "Put one there with your access_token and refresh_token.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String identifier = null;

        try {
            action = args[0];

        } catch (IndexOutOfBoundsException e) {
            usage();
            System.exit(1);
        }

        // TODO this should take a json file from the command line

        IndexTool index = new IndexTool(accessToken);
        index.tableId = tableId;
        try {
            if ("add".equals(action)) {

                identifier = args[1];
                Map<String, String> metadata = new HashMap<String, String>(7);
                try {
                    metadata = (Map) JSONValue.parse(new FileReader(new File(args[2])));
                } catch (IndexOutOfBoundsException e) {
                    usage();
                    System.exit(1);
                } catch (FileNotFoundException e) {
                    System.err.println("Couldn't find file " + args[2]);
                    usage();
                    System.exit(2);
                    e.printStackTrace();
                }
                index.addItem(identifier, metadata);
            } else if ("get".equals(action)) {
                identifier = args[1];
                index.getItem(identifier);
            } else if ("approve".equals(action)) {
                identifier = args[1];
                index.approveItem(identifier);
            } else if ("search".equals(action)) {
                Map<String,String> criteria = new HashMap<String, String>();
                criteria.put("file_type", "source");
                index.doSearch(criteria);

            } else if ("create".equals(action)) {
                StringBuffer schema = new StringBuffer();
                BufferedReader r = new BufferedReader(new FileReader(new File(args[1])));
                while (r.ready()) {
                    schema.append(r.readLine());
                }
                index.create(schema.toString());
            } else if ("modify".equals(action)) {
                StringBuffer schema = new StringBuffer();
                BufferedReader r = new BufferedReader(new FileReader(new File(args[1])));
                while (r.ready()) {
                    schema.append(r.readLine());
                }
                index.modify(schema.toString());
            } else if ("drop".equals(action)) {
                String tableIdToDrop = args[1];
                index.dropTable(tableIdToDrop);
            } else if ("list".equals(action)) {
                index.listTables();
            } else if ("generate_schema".equals(action)) {
                index.generateSchema();
            } else if ("dump".equals(action)) {
                String tableIdToDump = args[1];
                System.out.println(index.dumpTable(tableIdToDump));
            } else if ("load".equals(action)) {
                StringBuffer data = new StringBuffer();
                BufferedReader r = new BufferedReader(new FileReader(new File(args[2])));
                while (r.ready()) {
                    data.append(r.readLine());
                }
                String tableIdToLoad = args[1];
                index.loadData(tableIdToLoad, data.toString());
            }


        } catch (InvalidAccessTokenException e) {
            System.err.println("Invalid token; not caught by refresh code.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void approveItem(String identifier) {
        FusionIndex fi = getFusionIndex();
        Map<String,String> md = new HashMap<String, String>(1);
        md.put("date_approved",
                new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ssZ").format(new Date()));
        fi.update(identifier, md);
    }


    private IndexTool(String accessToken) {
        this.accessToken = accessToken;
    }
    private void addItem(String identifier, Map<String,String> metadata) {
        FusionIndex fi = getFusionIndex();
        fi.index(identifier, metadata);
    }

    private FusionIndex getFusionIndex(String tableId) {
        FusionIndex fi = new FusionIndex(accessToken);
        fi.setTableId(tableId);
        return fi;
    }

    private FusionIndex getFusionIndex() {
        return getFusionIndex(tableId);
    }

    private void getItem(String identifier) {
        FusionIndex fi = getFusionIndex();
        Map<String,String> md = fi.getItemMetadata(identifier);
        for (String k : md.keySet()) {
            System.out.println(String.format("%s: %s", k, md.get(k)));

        }
    }
    private void modify(String schema) {
       JSONObject current = getSchemaForTable(tableId);
       JSONObject target = (JSONObject) JSONValue.parse(schema);
       Map<String,JSONObject> currentCols = new HashMap<String, JSONObject>(10);
        Map<String,JSONObject> targetCols = new HashMap<String, JSONObject>(10);
       for (Object tmp : (JSONArray) current.get("columns")) {
           JSONObject col = (JSONObject) tmp;
           currentCols.put((String) col.get("name"), col);
       }
       for (Object tmp : (JSONArray) target.get("columns")) {
            JSONObject col = (JSONObject) tmp;
            targetCols.put((String) col.get("name"), col);
       }
       for (JSONObject col: targetCols.values()) {
           if (!currentCols.containsKey(col.get("name"))) {
               addColumn(col);
           }
       }
        for (JSONObject col: currentCols.values()) {
            if (!targetCols.containsKey(col.get("name"))) {
                removeColumn(col);
            }
        }
    }

    private void addColumn(JSONObject col) {
        changeTable("https://www.googleapis.com/fusiontables/v1/tables/" + tableId +
                    "/columns", col.toJSONString(), "POST");
    }
    private void removeColumn(JSONObject col) {
        changeTable("https://www.googleapis.com/fusiontables/v1/tables/" + tableId +
                "/columns/" + col.get("columnId"), col.toJSONString(), "DELETE");
    }

    private void create(String schema) {
        changeTable("https://www.googleapis.com/fusiontables/v1/tables", schema, "POST");
    }

    private JSONObject getSchemaForTable(String tableId) {

        HttpURLConnection cn = null;
        try {
            cn = Utils.gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/tables/" + tableId),
                    "GET", accessToken);
            if (cn.getResponseCode() != cn.HTTP_OK) {
                System.out.println(cn.getResponseCode());
                System.out.println(cn.getResponseMessage());
            } else {
                //return (JSONObject) JSONValue.parse(new InputStreamReader(cn.getInputStream()));
                String tmp  = Utils.readStream(cn.getInputStream());
                System.out.println(tmp);
                return (JSONObject) JSONValue.parse(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void changeTable(String url, String schema, String method) {
        try {
            HttpURLConnection cn = Utils.gapi_connect(new URL(url),
                    method, accessToken);
            cn.setRequestProperty("Content-Type", "application/json");
            if (method != "DELETE") {
                OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
                out.write(schema);
                out.flush();
                out.close();
            }
            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println(Utils.readStream(cn.getInputStream()));
            } else if  ("DELETE".equals(method) && cn.getResponseCode() == cn.HTTP_NO_CONTENT) {
                return;
            } else {
                System.err.println(cn.getResponseCode() + "\n" + cn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean dropTable(String tableId) {
        try {
            HttpURLConnection cn = Utils.gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/tables/" +
                    tableId),
                    "DELETE", accessToken);
            cn.setRequestProperty("Content-Type", "application/json");
            if (cn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void doSearch(Map<String, String> params) {
        FusionIndex fi = getFusionIndex();
        if (false) {
            for (String id : fi.search(params)) {
                System.out.println(id);
            }
        } else {
            fi.search(params, new Index.SearchResultProcessor() {
                @Override
                public void process(Map<String, String> result) {
                    System.out.println(result);
                }
            });
        }
    }

    private void listTables() {
        try {
            HttpURLConnection cn = Utils.gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/tables"),
                    "GET", accessToken);

            JSONObject o = (JSONObject) JSONValue.parse(new InputStreamReader(cn.getInputStream()));
            for (Map<String,Object> item : (List<Map<String, Object>>) o.get("items")) {
                System.out.println(item.get("name") + "\t" + item.get("tableId"));
                HttpURLConnection cn2 = Utils.gapi_connect(new URL("https://www.googleapis.com/drive/v2/files/" +
                        item.get("tableId") + "/permissions"), "GET", accessToken);
                cn2.setDoOutput(false);
                JSONObject o2 = (JSONObject) JSONValue.parse(new InputStreamReader(cn2.getInputStream()));

                for (Map<String,Object> item2 : (List<Map<String, Object>>) o2.get("items")) {
                    String name = (String) (item2.containsKey("name") ? item2.get("name") : item2.get("id"));
                    System.out.println(name + "\t" + item2.get("role"));
                }

                System.out.println("--------------------");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private void generateSchema() {
        JSONObject schema = new JSONObject();
        schema.put("name", "aikuma_metadata");
        schema.put("isExportable", false);
        schema.put("description", "Main metadata table for Aikuma cloud storage");
        JSONArray columns = new JSONArray();
        JSONObject id = new JSONObject();
        id.put("name", "identifier");
        id.put("type", "STRING");
        columns.add(id);
        for (FusionIndex.MetadataField f : FusionIndex.MetadataField.values()) {
            JSONObject tmp = new JSONObject();
            tmp.put("name", f.getName());
            tmp.put("type", f.getType());
            columns.add(tmp);
        }
        schema.put("columns", columns);
        System.out.println(schema.toJSONString());

    }

    private String dumpTable(String tableId) {
        HttpURLConnection cn = null;
        try {
            cn = Utils.gapi_connect(new URL("https://www.googleapis.com/fusiontables/v1/query?sql=SELECT+*+from+" + tableId +";"),
                    "GET", accessToken);
            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK)
                return  readStream(cn.getInputStream());
            else if (cn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.out.println("Invalid access token: " + this.accessToken);
                throw new InvalidAccessTokenException();
            } else {
                System.out.println(String.valueOf(cn.getResponseCode()));
                System.out.println(cn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadData(String tableId, String json) {
        JSONObject o = (JSONObject) JSONValue.parse(json);
        List<String> cols = (List<String>) o.get("columns");
        FusionIndex idx = getFusionIndex(tableId);
        for (Object tmp: (List) o.get("rows")) {
            List<String> row = (List<String>) tmp;
            Map<String, String> item = new HashMap<String, String>(cols.size());
            String identifier = "";
            for (int i = 0; i < row.size(); i++) {
                String key = cols.get(i);
                String val = row.get(i);

                if (val.length() == 0) continue;
                if (key.equals("identifier")) {
                    identifier = val;
                    continue;
                }

                FusionIndex.MetadataField field = FusionIndex.MetadataField.byName(key);
                String format = field.getFormat();
                if (format != null) {
                    SimpleDateFormat df = new SimpleDateFormat(format);
                    try {
                        df.parse(val);
                    } catch (ParseException e) {
                        continue;
                    }
                }
                if (field.isMultivalue()) {
                    val = val.replace('|', ',').substring(1, val.length() - 1);
                    if (val.length() == 0) {
                        val = "foo";
                    }
                }
                item.put(key, val);

            }
            idx.index(identifier, item);
            //System.out.println(item);
        }
    }
}
