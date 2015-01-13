package org.lp20.aikuma.servers.index_server;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.json.simple.JSONArray;
import org.lp20.aikuma.storage.FusionIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.storage.InvalidAccessTokenException;
import static org.lp20.aikuma.storage.Utils.validateIndexMetadata;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("index")
public class IndexResource {
    private final FusionIndex idx;
    private static final Logger log = Logger.getLogger(IndexResource.class.getName());

    private String table_id;
    private TokenManager tokenManager;

    public IndexResource(@Context Application app) {
        //TODO: is there another way to do this?
        IndexServerApplication a = (IndexServerApplication) app;
        table_id =  (String) a.getProperty("table_id");
        tokenManager = a.tokenManager;

        idx = new FusionIndex(tokenManager.getAccessToken());
        idx.setTableId(table_id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"authenticatedUser"})
    public Response search(MultivaluedMap<String, String> formParams,
                           @DefaultValue("false") @QueryParam("detail") String detail) {
        try {
            return doSearch(formParams, detail);
        } catch (InvalidAccessTokenException e1) {
            idx.setAccessToken(tokenManager.updateAccessToken());
            try {
                return doSearch(formParams, detail);
            } catch (InvalidAccessTokenException e2) {
                log.severe("Unable to refresh access_token successfully");
                return Response.serverError().build();
            }
        }
    }

    private Response doSearch(MultivaluedMap<String, String> formParams, String detail)  {
        Response resp;
        Map<String, String> params = makeMetadataMap(formParams);
        if (detail.equals("true")) {
            JSONArray data = new JSONArray();
            idx.search(params, new Index.SearchResultProcessor() {
                @Override
                public void process(Map<String, String> result) {
                    data.add(result);
                }
            });
            resp = Response.ok(data.toJSONString()).build();
        } else {
            resp = Response.ok(JSONValue.toJSONString(idx.search(params))).build();
        }
        return resp;
    }

    @GET
    @Path("{identifier}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"authenticatedUser"})
    public Response getItem(@PathParam("identifier") String identifier) {
        try {
            return doGetItem(identifier);
        } catch (InvalidAccessTokenException e) {
            idx.setAccessToken(tokenManager.updateAccessToken());
            try {
                return doGetItem(identifier);
            } catch (InvalidAccessTokenException ex) {
                log.severe("Unable to refresh access_token successfully");
                return Response.serverError().build();
            }
        }
    }

    private Response doGetItem(String identifier) {
        Response resp;
        Map<String, String> md = idx.getItemMetadata(identifier);
        if (md != null)  {
            resp = Response.ok(JSONValue.toJSONString(md)).build();
        }
        else {
            resp = Response.status(404).build();
        }
        return resp;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{identifier}")
    @RolesAllowed({"authenticatedUser"})
    public Response addItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        try {
            return doAddItem(identifier, formParams);
        } catch (InvalidAccessTokenException e) {
            idx.setAccessToken(tokenManager.updateAccessToken());
            try {
                return doAddItem(identifier, formParams);
            } catch (InvalidAccessTokenException e1) {
                log.severe("Unable to refresh access_token successfully");
                return Response.serverError().build();
            }
        }
    }

    private Response doAddItem(String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);
        String msg = validateIndexMetadata(data, true);
        if (msg.length() != 0) {
            return Response.status(new ErrorStatus(400, msg.replace('\n', ';'))).build();
        }
        try {
            if (idx.index(identifier, data)) {
                return Response.accepted().build();
            } else {
                return Response.serverError().build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(new ErrorStatus(400, e.getMessage())).build();
        }
    }

    @PUT
    @Path("{identifier}")
    @RolesAllowed({"authenticatedUser"})
    public Response updateItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        try {
            return doUpdateItem(identifier, formParams);
        } catch (InvalidAccessTokenException e) {
            idx.setAccessToken(tokenManager.updateAccessToken());
            try {
                return doUpdateItem(identifier, formParams);
            } catch (InvalidAccessTokenException e1) {
                log.severe("Invalid access token; work out this logic, Bob");
                return Response.serverError().build();
            }
        }
    }

    private Response doUpdateItem(String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);
        String msg = validateIndexMetadata(data, false);
        if (msg.length() != 0) {
            return Response.status(new ErrorStatus(400, msg)).build();
        }
        try {
            idx.update(identifier, data);
            return Response.accepted().build();
        } catch (IllegalArgumentException e) {
            return Response.status(new ErrorStatus(400, e.getMessage())).build();
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
