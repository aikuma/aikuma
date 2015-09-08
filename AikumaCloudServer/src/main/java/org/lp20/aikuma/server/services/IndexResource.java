package org.lp20.aikuma.server.services;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.json.simple.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.storage.google.GoogleDriveIndex;
import org.lp20.aikuma.storage.InvalidAccessTokenException;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.google.GoogleDriveStorage;
import static org.lp20.aikuma.storage.Utils.validateIndexMetadata;
import org.lp20.aikuma.server.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("index")
public class IndexResource {
    private final GoogleDriveIndex idx;
    private static final Logger log = Logger.getLogger(IndexResource.class.getName());

    private String gdRootName;
    private TokenManager tokenManager;
    private UpdateNotifier updateNotifier;

    public IndexResource(@Context Application app) {
        //TODO: is there another way to do this?
        IndexServerApplication a = (IndexServerApplication) app;
        gdRootName =  (String) a.getProperty("aikuma_root_name");
        tokenManager = a.tokenManager;

        try {
            GoogleDriveStorage gd = new GoogleDriveStorage(
                    tokenManager.getAccessToken(),
                    (String) a.getProperty("aikuma_root_id"),
                    "");
            updateNotifier = new UpdateNotifier(tokenManager, gd, a.gcmServer);
        } catch (DataStore.StorageException e) {
            log.warning("failed to initialize update notifier due to google drive storage error: " + e.getMessage());
            updateNotifier = null;
        }

        idx = new GoogleDriveIndex(gdRootName, new org.lp20.aikuma.storage.google.TokenManager() {
            @Override
            public String accessToken() {
                return tokenManager.getAccessToken();
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"authenticatedUser"})
    public Response search(MultivaluedMap<String, String> formParams,
                           @DefaultValue("false") @QueryParam("detail") String detail) {
        return doSearch(formParams, detail);
    }

    private Response doSearch(MultivaluedMap<String, String> formParams, String detail)  {
        Response resp;
        Map<String, String> params = makeMetadataMap(formParams);
        if (detail.equals("true")) {
            JSONArray data = new JSONArray();
            idx.search(params, new Index.SearchResultProcessor() {
                @Override
                public boolean process(Map<String, String> result) {
                    data.add(result);
                    return true;
                }
            });
            resp = Response.ok(data.toJSONString()).build();
        } else {
            resp = Response.ok(JSONValue.toJSONString(idx.search(params))).build();
        }
        return resp;
    }

    @GET
    @Path("{identifier: .+}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"authenticatedUser"})
    public Response getItem(@PathParam("identifier") String identifier) {
        return doGetItem(identifier);
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
    @Path("{identifier: .+}")
    @RolesAllowed({"authenticatedUser"})
    public Response addItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        /* TODO: not ready for production
        if (updateNotifier != null)
            updateNotifier.processNewFile(identifier);
        */
        return doAddItem(identifier, formParams);
    }

    private Response doAddItem(String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);
        //String msg = validateIndexMetadata(data, true);
	String msg = "";
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
    @Path("{identifier: .+}")
    @RolesAllowed({"authenticatedUser"})
    public Response updateItem(@PathParam("identifier") String identifier, MultivaluedMap<String, String> formParams) {
        return doUpdateItem(identifier, formParams);
    }

    private Response doUpdateItem(String identifier, MultivaluedMap<String, String> formParams) {
        Map<String, String> data = makeMetadataMap(formParams);
        //String msg = validateIndexMetadata(data, false);
	String msg = "";
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
