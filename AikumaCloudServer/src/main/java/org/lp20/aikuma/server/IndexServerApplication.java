package org.lp20.aikuma.server;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by bob on 10/30/14.
 */
public class IndexServerApplication extends ResourceConfig{
    public IndexServerApplication() {

    }
    public TokenManager tokenManager;
    public JWTVerifier jwtVerifier = null;

}
