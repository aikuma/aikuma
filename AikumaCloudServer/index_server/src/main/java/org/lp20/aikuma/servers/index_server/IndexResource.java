package org.lp20.aikuma.servers.index_server;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.glassfish.jersey.server.ResourceConfig;
import org.lp20.aikuma.storage.FusionIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.GoogleAuth;
import org.lp20.aikuma.storage.InvalidAccessTokenException;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("index")
public class IndexResource {
    private final FusionIndex idx;
    private static final Logger log = Logger.getLogger(IndexResource.class.getName());

    private String access_token;
    private String refresh_token;
    private String table_id;
    private String client_id;
    private String client_secret;

    public IndexResource(@Context Application app) {
        //TODO: is there another way to do this?
        ResourceConfig rc = (ResourceConfig) app;
        access_token = (String) rc.getProperty("access_token");
        refresh_token = (String) rc.getProperty("refresh_token");
        table_id =  (String) rc.getProperty("table_id");
        client_id = (String) rc.getProperty("client_id");
        client_secret = (String) rc.getProperty("client_secret");

        idx = new FusionIndex(access_token);
        idx.setTableId(table_id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(MultivaluedMap<String, String> formParams) {
        return Response.status(new ErrorStatus(500, "Not implemented yet.")).build();
    }

    @GET
    @Path("{identifier}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(@PathParam("identifier") String identifier) {
        Response resp;
        try {
            Map<String, String> md = idx.getItemMetadata(identifier);
            if (md != null)  {
                resp = Response.ok(JSONValue.toJSONString(md)).build();
            }
            else {
                resp = Response.status(404).build();
            }
        } catch (InvalidAccessTokenException e) {
            log.warning(e.toString());
            resp = Response.serverError().build();

        }
        return resp;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{identifier}")
    public Response addItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);

        try {
            if (idx.index(identifier, data)) {
                return Response.accepted().build();
            } else {
                return Response.serverError().build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(new ErrorStatus(400, e.getMessage())).build();
        } catch (InvalidAccessTokenException e) {
            log.severe("Invalid access token; work out this logic, Bob");
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("{identifier}")
    public Response updateItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);

        try {
            idx.update(identifier, data);
            return Response.accepted().build();
        } catch (IllegalArgumentException e) {
            return Response.status(new ErrorStatus(400, e.getMessage())).build();
        } catch (InvalidAccessTokenException e) {
            log.severe("Invalid access token; work out this logic, Bob");
            return Response.serverError().build();
        }
    }

    private boolean refreshToken() {
        log.warning("Invalid access_token; attempting to refresh");
        GoogleAuth ga = new GoogleAuth(client_id, client_secret);
        if (ga.refreshAccessToken(refresh_token)) {
            access_token = ga.getAccessToken();
            return true;
        } else {
            log.severe("Unable to refresh access_token");
            return false;
        }
    }

    private static Map<String, String> makeMetadataMap(MultivaluedMap<String, String> formParams) {
        Map<String, String> data = new HashMap<>(10);
        for (Map.Entry<String, List<String>> e : formParams.entrySet()) {
            for (String s : e.getValue()) {
                data.put(e.getKey(), s);
                break; // If there's multiple values for a key, ignore subsequent ones
            }
        }
        return data;
    }

    private static class ErrorStatus implements Response.StatusType {
        int code;
        String msg;
        ErrorStatus(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase() {
            return msg;
        }
    }
}
