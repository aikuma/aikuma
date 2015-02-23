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
import org.lp20.aikuma.storage.*;
import static org.lp20.aikuma.storage.Utils.validateIndexMetadata;
import org.lp20.aikuma.server.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("file")
public class FileServices {
    private static final Logger log = Logger.getLogger(IndexResource.class.getName());

    private String mRootId;
    private TokenManager mTm;

    public FileServices(@Context Application app) {
        //TODO: is there another way to do this?
        IndexServerApplication a = (IndexServerApplication) app;
	mRootId = (String) a.getProperty("aikuma_root_id");
        mTm = a.tokenManager;
    }

    @PUT
    @Path("{identifier: .+}/share/{email: [^/]+}")
    //@RolesAllowed({"authenticatedUser"})
    public Response shareFile(
            @PathParam("identifier") String identifier,
            @PathParam("email") String email
            ) {
        GoogleDriveStorage gd;
        try {
            gd = new GoogleDriveStorage(
                    mTm.getAccessToken(),
                    mRootId,
                    email);
        } catch (DataStore.StorageException err) {
            return Response.serverError().build();
        }
        if (gd.share(identifier)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
