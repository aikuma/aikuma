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
import org.json.simple.JSONObject;
import org.lp20.aikuma.storage.*;
import org.lp20.aikuma.storage.google.Api;
import org.lp20.aikuma.storage.google.Search;
import org.lp20.aikuma.storage.google.GoogleDriveStorage;
import static org.lp20.aikuma.storage.Utils.validateIndexMetadata;
import org.lp20.aikuma.server.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("gdindex")
public class GdIndexServices {
    private static final Logger log = Logger.getLogger(GdIndexServices.class.getName());

    private String mRootFileId;
    private Api mApi;

    public GdIndexServices(@Context Application app) {
        //TODO: is there another way to do this?
        IndexServerApplication a = (IndexServerApplication) app;
	String rootName = (String) a.getProperty("aikuma_root_name");
        mApi = new Api(new org.lp20.aikuma.storage.google.TokenManager() {
            @Override
            public String accessToken() {
                return a.tokenManager.getAccessToken();
            }
        });
        String q = "title = '%s' and 'root' in parents and trashed = false";
        Search search = mApi.search(String.format(q, rootName.replaceAll("'", "\\\\'")));
        if (search.hasMoreElements()) {
            try {
                mRootFileId = (String) ((JSONObject) search.nextElement()).get("id");
            } catch (Search.Error err) {
                mRootFileId = null;
            }
        } else {
            mRootFileId = null;
        }
    }

    @PUT
    @Path("share/{email: [^/]+}")
    @RolesAllowed({"authenticatedUser"})
    public Response shareCentralGoogleDrive(@PathParam("email") String email) {
        if (mRootFileId == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        JSONObject obj = mApi.shareWith(mRootFileId, email);
        if (obj == null) {
            return Response.serverError().build();
        } else {
            return Response.ok().build();
        }
    }
}
