package org.lp20.aikuma.server;

import java.util.logging.Logger;
import org.lp20.aikuma.storage.GoogleAuth;

public class TokenManagerForNativeApp implements TokenManager {
    private static Logger log = Logger.getLogger(TokenManagerForNativeApp.class.getName());

    private GoogleAuth mAuth;
    private String mAccessToken;


    public TokenManagerForNativeApp(String clientId, String clientSecret, String refreshToken) {
        mAuth = new GoogleAuth(clientId, clientSecret);
        if (mAuth.refreshAccessToken(refreshToken)) {
            mAccessToken = mAuth.getAccessToken();
        } else {
            mAccessToken = null;
        }
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public String refreshAccessToken() {
        if (mAuth.refreshAccessToken(mAuth.getRefreshToken())) {
            mAccessToken = mAuth.getAccessToken();
        } else {
            mAccessToken = null;
        }
        return mAccessToken;
    }

}
