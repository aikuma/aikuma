package org.lp20.aikuma.servers.index_server;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by bob on 10/30/14.
 */
public class IndexServerApplication extends ResourceConfig{
    public TokenManager tokenManager;
    public JWTVerifier jwtVerifier = null;

}
