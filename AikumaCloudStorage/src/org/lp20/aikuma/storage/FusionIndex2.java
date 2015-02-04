package org.lp20.aikuma.storage;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * FusionIndex2 rewrites "write" methods of FusionIndex so that table-changing
 * requests are serviced by the private index server.
 *
 * The constructor requires base URL of the server and an ID token of the
 * client Android app. The token is passed to the server which verifies that
 * the request is coming from the registered app.
 *
 * Other requests are handled as usual -- the client passes an access token
 * which is used to access the FusionTables table.
 */
public class FusionIndex2 extends FusionIndex {
    String baseUrl;
    String accessToken;
    String idToken;

    /**
     * @param baseUrl Base URL for the index REST API.
     * @param idToken ID token obtained by the client Android app.
     * @param accessToken Token for accessing FusionTables.
     */
    public FusionIndex2(String baseUrl, String idToken, String accessToken) {
        super(accessToken);
        this.baseUrl = baseUrl;
        this.idToken = idToken;
    }

    @Override
    public boolean index(String identifier, Map<String,String> metadata) {
        return call("POST", identifier, metadata);
    }

    @Override
    public boolean update(String identifier, Map<String,String> metadata) {
        return call("PUT", identifier, metadata);
    }

    private boolean call(String method,
                         String identifier,
                         Map<String,String> metadata) {
        if (identifier == null || metadata == null) return false;
        try {
            HttpURLConnection con = mkCon(method, baseUrl + "/" + identifier);
            writePostData(con, Utils.map2query(metadata));
            switch (con.getResponseCode()) {
            case 202:
                return true;
            case 404:
            case 500:
            default:
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private HttpURLConnection mkCon(String method, String urlStr)
        throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(true);
        con.setDoOutput(method.equals("POST") || method.equals("PUT"));
        con.setRequestMethod(method);
        con.addRequestProperty("X-Aikuma-Auth-Token", idToken);
        return con;
    }

    private void writePostData(HttpURLConnection con, String data)
        throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(data);
        out.flush();
        out.close();
    }
}
