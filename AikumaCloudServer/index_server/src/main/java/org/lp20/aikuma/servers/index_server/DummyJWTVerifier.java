package org.lp20.aikuma.servers.index_server;

/**
 * Created by bob on 11/12/14.
 */
public class DummyJWTVerifier implements JWTVerifier {

    /**
     * See {@link org.lp20.aikuma.servers.index_server.JWTVerifier}
     * NB - always returns true
     *
     * @param token The token to verify; doesn't matter what it is
     * @return true
     */
    @Override
    public boolean verifyToken(String token) {
        return true;
    }
}
