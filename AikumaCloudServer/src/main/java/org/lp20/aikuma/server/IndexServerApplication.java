package org.lp20.aikuma.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.lp20.aikuma.server.gcm.GcmServer;
import org.mapdb.DB;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created by bob on 10/30/14.
 */
public class IndexServerApplication extends ResourceConfig{
    public IndexServerApplication() {

    }
    public TokenManager tokenManager;
    public JWTVerifier jwtVerifier = null;
    public DB db;
    public GcmServer gcmServer;

    public ConcurrentNavigableMap<String,String> getGcmRegIdsMap() {
        return db.getTreeMap("gcm_reg_ids");
    }
}
