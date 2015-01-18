package org.lp20.aikuma.storage;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * FusionIndex2 rewrites "write" methods so that table-changing requests are
 * serviced by the private index server.
 */
public class FusionIndex2 extends FusionIndex {
    String baseUrl;
    String accessToken;

    /**
     * @param accessToken Token for accessing FusionTables.
     * @param baseUrl Base URL for the index REST API.
     */
    public FusionIndex2(String baseUrl, String accessToken) {
        super(accessToken);
        this.baseUrl = baseUrl;
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
            System.out.println("aaa");
            int x = con.getResponseCode();
            System.out.println(x);
            switch (x) {
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
        con.addRequestProperty("X-Aikuma-Auth-Token", accessToken);
        return con;
    }

    private void writePostData(HttpURLConnection con, String data)
        throws IOException {
        try {
        new OutputStreamWriter(con.getOutputStream()).write(data);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
