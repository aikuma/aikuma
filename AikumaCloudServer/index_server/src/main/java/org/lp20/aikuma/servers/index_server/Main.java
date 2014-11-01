package org.lp20.aikuma.servers.index_server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";


    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static String accessToken;
    private static String refreshToken;
    private static String tableId;

    private static boolean setup(IndexServerApplication app) {
        Properties props = new Properties();
        String fileLoc = System.getProperty("user.dir");
        fileLoc += System.getProperty("file.separator");
        fileLoc += "fusion_index.properties";
        try {
            InputStream in = new FileInputStream(fileLoc);
            props.load(in);
            in.close();
            Map<String,Object> tmp = new HashMap<String, Object>(5);
            tmp.put("table_id", props.getProperty("table_id"));

            app.tokenManager = new TokenManager(props.getProperty("client_id"),
                                                props.getProperty("client_secret"),
                                                props.getProperty("refresh_token"),
                                                props.getProperty("access_token"));

            app.addProperties(tmp);
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
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), app);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();
    }


}

