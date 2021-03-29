package org.jivesoftware.openfire.plugin.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.*;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.util.*;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import org.igniterealtime.openfire.plugins.pushnotification.PushInterceptor;
import org.ifsoft.sso.Password;

/**
 * The Class AuthFilter.
 */
public class AuthFilter implements ContainerRequestFilter {

    private static Logger LOG = LoggerFactory.getLogger(AuthFilter.class);
    @Context private HttpServletRequest httpRequest;

    @Override public ContainerRequest filter(ContainerRequest containerRequest) throws WebApplicationException
    {
        // Let the preflight request through the authentication
        if ("OPTIONS".equals(containerRequest.getMethod())) {
            return containerRequest;
        }
		
        // Let web authn through
        if (containerRequest.getPath().contains("/meet/webauthn")) {
            return containerRequest;
        }		

        // Get the authentification passed in HTTP headers parameters
        String auth = containerRequest.getHeaderValue("authorization");

        if (auth == null) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        // let replies through

        if (PushInterceptor.tokens.containsKey(auth))
        {
            return containerRequest;
        }

        String[] usernameAndPassword = BasicAuth.decode(auth);

        // If username or password fail
        if (usernameAndPassword == null || usernameAndPassword.length != 2) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

/*
        boolean userAdmin = AdminManager.getInstance().isUserAdmin(usernameAndPassword[0], true);

        if (!userAdmin) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
*/
        try {
			boolean ofmeetWebAuthnEnabled = JiveGlobals.getBooleanProperty( "ofmeet.webauthn.enabled", false );	

			if (ofmeetWebAuthnEnabled)
			{
				User user = XMPPServer.getInstance().getUserManager().getUser(usernameAndPassword[0]);
				
                if (user.getProperties().containsKey("webauthn-key-" + usernameAndPassword[1]))
                {
					return containerRequest;
                }				
			}
			else
				
			if (Password.passwords.containsKey(usernameAndPassword[0]))
			{
				String passkey = Password.passwords.get(usernameAndPassword[0]).trim();	

				if (passkey.equals(usernameAndPassword[1]))
				{
					return containerRequest;
				}				
			}				
				
            AuthFactory.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
        } catch (Exception e) {
            LOG.error("Wrong HTTP Basic Auth authorization", e);
        }

        return containerRequest;
    }
}
