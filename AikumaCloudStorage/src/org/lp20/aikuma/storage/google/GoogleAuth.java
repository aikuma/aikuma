package org.lp20.aikuma.storage.google;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.simple.*;
import org.lp20.aikuma.storage.*;

/**
 * Utility class for handling google authentication/authorization.
 * 
 * @author haejoong
 */
public class GoogleAuth {
    private String clientId_;
    private String clientSecret_;
    private String lastError_;
    private String accessToken_;
    private String refreshToken_;
    private long timeExpired_;
	
    /**
     * Construct an authentication/authorization object. Example,
     * 
     * - Call getAuthUrl(), and open an authentication/authorization page
     *   pointed by the returned URL.
     * - When submitted the form on the page, the browser displays a string
     *   called authorization code.
     * - Call requestAccessToken() method with the authorization code.
     * - Call getAccessToken() method to get the access token.
     * 
     * @param clientId
     * @param clientSecret
     */
    public GoogleAuth(String clientId, String clientSecret) {
        clientId_ = clientId;
        clientSecret_ = clientSecret;
        timeExpired_ = 0;
    }

    /**
     * Returns a URL that the end user needs to visit to authenticate and authorize.
     * In the page, user logs in to google and authorize APIs. Then, the page
     * returns an authorization code.
     * 
     * @return A URL in string.
     */
    public String getAuthUrl(List<String> apis) {
            String url = "https://accounts.google.com/o/oauth2/auth?"
                            + "response_type=code&"
                            + "redirect_uri=urn:ietf:wg:oauth:2.0:oob&"
                            + "client_id=" + clientId_;

        if (apis.size() > 0) {
            url += "&scope=";
            String tmp = "";
            for (String api: apis) {
                tmp += api + " ";
            }
            try {
                url += URLEncoder.encode(tmp.trim(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
            
        return url;
    }

    /**
     * Check to see if an access_token is valid
     *
     * @param accessToken an OATH2 access token
     * @return true if valid, false otherwise
     */
    public static boolean validateAccessToken(String accessToken) {

        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v2/tokeninfo");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write("access_token="+ accessToken);
            writer.flush();
            writer.close();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else if (con.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                return false;
            } else {
                System.err.println(con.getResponseCode());
                System.err.println(con.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtain an access_token given a refresh_token. Use getAccessToken() to get the obtained token.
     *
     * @param refreshToken Google supplied OATH2.0 refresh_token
     * @return true if successful, false, otherwise
     */
    public boolean refreshAccessToken(String refreshToken) {
        refreshToken_ = refreshToken;
        return handleTokenRequest("grant_type=refresh_token" +
                "&refresh_token=" + refreshToken +
                "&client_id=" + clientId_ +
                "&client_secret=" + clientSecret_);
    }

    /**
     * Obtain an access token. Use getAccessToken() to get the obtained token.
     *
     * @param authCode
     * @return true if successful, false, otherwise
     */
    public boolean requestAccessToken(String authCode) {
        return handleTokenRequest("redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                "&grant_type=authorization_code" +
                "&code=" + authCode +
                "&client_id=" + clientId_ +
                "&client_secret=" + clientSecret_);
    }

    private boolean handleTokenRequest(String body) {
        try {
            URL url = new URL("https://accounts.google.com/o/oauth2/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                parseTokenResponse(Utils.readStream(con.getInputStream()));
                lastError_ = null;
                return true;
            } else {
                // TODO: Error logging?
                accessToken_ = null;
                lastError_ = "HTTP error: " + con.getResponseMessage();
                return false;
            }
        }
        catch (IOException e) {
            // TODO: Logging? e.printStackTrace();
            accessToken_ = null;
            lastError_ = "Exception: " + e.getMessage();
            return false;
        }
    }

    private void parseTokenResponse(String resp) {
        JSONObject obj = (JSONObject) JSONValue.parse(resp);
        long n = ((Long) obj.get("expires_in")).longValue();
        timeExpired_ = new Date().getTime() + n - 60000;
        accessToken_ = (String) obj.get("access_token");
        if (obj.containsKey("refresh_token"))
            refreshToken_ = (String) obj.get("refresh_token");

    }


    /**
     * Return the access token obtained by requestAccessToken().
     * Return null if there is nothing to return.
     * 
     * @return String token if token exists, null otherwise.
     */
    public String getAccessToken() {
        if (timeExpired_ < new Date().getTime()) {
            if (refreshToken_ == null || !refreshAccessToken(refreshToken_))
                return null;
        }
        return accessToken_;
    }
    
    public String getRefreshToken() {
        return refreshToken_;
    }

    /**
     * Returns an error message produced by a previous method call that
     * is involved in communication with google auth servers.
     * 
     * @return Error message.
     */
    public String getLastError() {
            return lastError_;
    }
}
