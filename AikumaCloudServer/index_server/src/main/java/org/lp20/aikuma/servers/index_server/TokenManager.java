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
    private GoogleServiceAuth auth;

    private String access_token;
    private String scopes;
    private static Logger log = Logger.getLogger(TokenManager.class.getName());


    public TokenManager(String service_email, String scopes, String privateKeyPath, String password) {
        this.scopes = scopes;
        auth = new GoogleServiceAuth(service_email, privateKeyPath, password);
        this.access_token = auth.getAccessToken(scopes);
    }


    public String getAccessToken() {
        return access_token;
    }

    public String updateAccessToken() {
        String token = auth.getAccessToken(scopes);
        if (token == null) {
            log.severe("Unable to refresh access token");
            // I should probably just kill the app here, there's no way forward
            System.err.println("Fatal problem: invalid access token and can't refresh token");
            System.exit(2);
        }
        return token;
    }

}
