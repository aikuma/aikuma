package org.lp20.aikuma.server;

import java.util.logging.Logger;

/**
 * Obtain and refresh access token.
 */
public interface TokenManager {
    /**
     * Get access token.
     * @return Access token, or null if fails.
     */
    public String getAccessToken();

    /**
     * Refresh access token.
     * @return Access token, or null if fails.
     */
    public String refreshAccessToken();
}
