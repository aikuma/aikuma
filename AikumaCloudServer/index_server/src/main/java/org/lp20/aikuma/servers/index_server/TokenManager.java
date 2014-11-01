package org.lp20.aikuma.servers.index_server;


import org.lp20.aikuma.storage.GoogleAuth;

import java.util.logging.Logger;

/**
 * Created by bob on 10/30/14.
 *
 * Manages updating Google OAUTH Tokens
 *
 */
public class TokenManager {
    private GoogleAuth auth;

    private final String refresh_token;
    private String access_token;
    private static Logger log = Logger.getLogger(TokenManager.class.getName());


    public TokenManager(String client_id, String client_secret, String refresh_token, String access_token) {
        auth = new GoogleAuth(client_id, client_secret);
        this.refresh_token = refresh_token;
        this.access_token = access_token;
        if (!GoogleAuth.validateAccessToken(this.access_token)) {
            this.access_token = updateAccessToken();
        }
    }


    public String getAccessToken() {
        return access_token;
    }

    public String updateAccessToken() {
        if (auth.refreshAccessToken(this.refresh_token)) {
            return auth.getAccessToken();
        } else {
            log.severe("Unable to refresh access token");
            // I should probably just kill the app here, there's no way forward
            System.err.println("Fatal problem: invalid access token and can't refresh token");
            System.exit(2);
        }
        return null;
    }

}
