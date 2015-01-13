package org.lp20.aikuma.servers.index_server;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;

/**
 * Created by bob on 11/12/14.
 */
@Provider
@PreMatching
public class SecurityRequestFilter implements ContainerRequestFilter {
    public static String AUTH_HEADER_NAME = "X-Aikuma-Auth-Token";

    private final JWTVerifier jwtVerifier;
    private static Logger log = Logger.getLogger(SecurityRequestFilter.class.getName());

    public SecurityRequestFilter(@Context Application app) {
        this.jwtVerifier = ((IndexServerApplication) app).jwtVerifier;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String token = containerRequestContext.getHeaderString(AUTH_HEADER_NAME);
        log.info(token);
        boolean secure = containerRequestContext.getUriInfo().getBaseUri().getScheme().equals("https");
        containerRequestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return "JWTFilter";
                    }

                };
            }

            @Override
            public boolean isUserInRole(String s) {
                if ("authenticatedUser".equals(s) && jwtVerifier.verifyToken(token)) {
                    log.info("ok");
                    return true;
                } else {
                    log.info("nope");
                    return false;
                }

            }

            @Override
            public boolean isSecure() {
                return secure;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        });
    }
}
