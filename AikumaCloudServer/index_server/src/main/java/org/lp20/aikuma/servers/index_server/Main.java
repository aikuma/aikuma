package org.lp20.aikuma.servers.index_server;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ContainerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static boolean setup(IndexServerApplication app) {
        Properties props = new Properties();
        String fileLoc = System.getProperty("user.dir");
        fileLoc += System.getProperty("file.separator");
        fileLoc += "index_server.properties";
        try {
            InputStream in = new FileInputStream(fileLoc);
            props.load(in);
            in.close();
            Map<String,Object> localProps = new HashMap<String, Object>(5);
            localProps.put("table_id", props.getProperty("table_id"));
            localProps.put("base_uri", props.getProperty("base_uri"));
            localProps.put("use_ssl", props.getProperty("use_ssl"));
            localProps.put("keystore_file", props.getProperty("keystore_file"));
            localProps.put("keystore_password", props.getProperty("keystore_password"));

            app.tokenManager = new TokenManager(props.getProperty("client_id"),
                                                props.getProperty("client_secret"),
                                                props.getProperty("refresh_token"),
                                                props.getProperty("access_token"));
            if ("yes".equals(props.getProperty("require_auth"))) {
                localProps.put("require_auth", "yes");
                String audience = props.getProperty("client_id");
                List<String> client_ids = new LinkedList<String>();
                String tmp = props.getProperty("valid_app_client_ids");
                if (tmp != null) {
                    for (String s : tmp.split(",")) {
                        client_ids.add(s.replace("\\s*", ""));
                    }

                    if (!"yes".equals(localProps.get("use_ssl"))) {
                        log.warning("require_auth = yes, but use_ssl != yes; this is insecure!");
                    }

                    app.jwtVerifier = new GoogleJWTVerifier(audience, client_ids);
                } else {
                    log.severe("require_auth == yes, but no valid_app_client_ids specified. Aborting.");
                    System.exit(1);
                }
            } else {
                localProps.put("require_auth", "no");
                app.jwtVerifier = new DummyJWTVerifier();
            }
            app.addProperties(localProps);
        } catch (NullPointerException | IOException e) {
            log.severe("Unable to load FusionIndex config from " + fileLoc + ". Cannot continue.");
            return false;
        }
        return true;
    }


    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in org.lp20.aikuma package
        //final IndexServerApplication rc = new IndexServerApplication().packages("org.lp20.aikuma");
        final IndexServerApplication app = new IndexServerApplication();
        app.packages("org.lp20.aikuma.servers.index_server");

        if (!setup(app)) {
            System.err.println("Fatal configuration error");
            System.exit(1);
        }
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server;
        URI base_uri = URI.create((String) app.getProperty("base_uri"));

        if (((String) app.getProperty("use_ssl")).equals("yes")) {
            SSLContextConfigurator scc = new SSLContextConfigurator();
            URL url = null;
            try {
                url = new URL((String) app.getProperty("keystore_file"));
            } catch (MalformedURLException e) {
                log.severe("Unable to find keystore file; can't continue");
                System.exit(1);
            }
            scc.setKeyStoreFile(url.getFile());
            scc.setKeyStorePass((String) app.getProperty("keystore_password"));

             server = GrizzlyHttpServerFactory.createHttpServer(base_uri,
                    app,
                    true,
                    new SSLEngineConfigurator(scc.createSSLContext(), false, false, false));
        } else {
             server = GrizzlyHttpServerFactory.createHttpServer(base_uri, app);
        }
        log.info("Starting server at " + base_uri + "; ssl enabled = " + app.getProperty("use_ssl"));
        return server;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        final HttpServer server = startServer();
        long startup = System.currentTimeMillis() - startTime;

        System.out.println("Application started in " + startup + " milliseconds");
        while (true) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                server.shutdownNow();
            }
        }
    }


}

