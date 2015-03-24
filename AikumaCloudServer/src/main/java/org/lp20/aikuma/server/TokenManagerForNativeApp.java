package org.lp20.aikuma.server;

import java.util.logging.Logger;
import org.lp20.aikuma.storage.google.GoogleAuth;

public class TokenManagerForNativeApp implements TokenManager {
    private static Logger log = Logger.getLogger(TokenManagerForNativeApp.class.getName());

    private GoogleAuth mAuth;

    public TokenManagerForNativeApp(String clientId, String clientSecret, String refreshToken) {
        mAuth = new GoogleAuth(clientId, clientSecret);
        mAuth.refreshAccessToken(refreshToken);
    }

    @Override
    public String getAccessToken() {
        return mAuth.getAccessToken();
    }

    @Override
    public String refreshAccessToken() {
        return getAccessToken();
    }
}
