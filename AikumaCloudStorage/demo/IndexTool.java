import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleAuth;
import org.lp20.aikuma.storage.InvalidAccessTokenException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by bob on 6/4/14.
 *
 * A demo of adding a new FusionIndex item
 */
public class IndexTool {
    String accessToken;

    Map<String,String> metadata;
    String identifier;

    public static void usage() {
        System.err.println("Usage: IndexTool <action> <identifier> [data.json]");

    }
    public static void main(String[] args) {
        String accessToken = null;
        String refreshToken = null;
        String action = null;

        String client_id = "119115083785-cqbtnha90hobui893c0lc33olghb4uuv.apps.googleusercontent.com";
        String client_secret = "4Yz3JDpqbyf6q-uw0rP2BSnN";
        GoogleAuth auth = new GoogleAuth(client_id, client_secret);

        try {
            Properties config = new Properties();
            config.load(new FileInputStream(new File(System.getProperty("user.home") + "/.aikuma")));
            accessToken = config.getProperty("access_token");
            refreshToken = config.getProperty("refresh_token");

            if (!auth.validateAccessToken(accessToken)) {
                auth.refreshAccessToken(refreshToken);
                accessToken = auth.getAccessToken();
                //config.setProperty("access_token", accessToken);
                config.put("access_token", accessToken);
                config.store(new FileOutputStream(new File(System.getProperty("user.home") + "/.aikuma")), "Automatically updated with new access_token");
            }

        } catch (FileNotFoundException e) {
            System.err.println("No .aikuma prefs file found in your home directory.\n" +
                    "           Put one there with your access_token and refresh_token.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String identifier = null;

        try {
            action = args[0];
            identifier = args[1];

        } catch (IndexOutOfBoundsException e) {
            usage();
            System.exit(1);
        }

        // TODO this should take a json file from the command line

        IndexTool index = new IndexTool(accessToken);
        try {
            if ("add".equals(action)) {
                Map<String, String> metadata = new HashMap<String, String>(7);
                try {
                    metadata = (Map) JSONValue.parse(new FileReader(new File(args[2])));
                } catch (IndexOutOfBoundsException e) {
                    usage();
                    System.exit(1);
                } catch (FileNotFoundException e) {
                    usage();
                    System.exit(2);
                    e.printStackTrace();
                }
                index.addItem(identifier, metadata);
            } else if ("get".equals(action))
                index.getItem(identifier);
            else if ("search".equals(action))
                System.out.println("Not implemented (yet)");

        } catch (InvalidAccessTokenException e) {
            System.err.println("Invalid token; not caught by refresh code.");
        }
    }



    private IndexTool(String accessToken) {
        this.accessToken = accessToken;
    }
    private void addItem(String identifier, Map<String,String> metadata) {
        FusionIndex fi = new FusionIndex(accessToken);
        fi.index(identifier, metadata);
    }
    private void getItem(String identifier) {
        FusionIndex fi = new FusionIndex(accessToken);
        Map<String,String> md = fi.get_item_metadata(identifier);
        for (String k : md.keySet()) {
            System.out.println(String.format("%s: %s", k, md.get(k)));

        }
    }
    private void doSearch(String params) {

    }
}
