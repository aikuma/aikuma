package org.lp20.aikuma.servers.index_server;

/**
 * JWTVerifiers check JSON Web Tokens for validity
 *
 * @author rparkerldc@gmail.com
 */
public interface JWTVerifier {
    /**
     * Verify that a token is valid by checking against a certificate key
     * @param token the token to check
     * @return true if valid, false otherwise
     */
    boolean verifyToken(String token);
}
