/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.jaxrs.services.tests;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.AllowAllAuthenticator;
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.ForbidAllAuthenticator;
import org.restlet.test.jaxrs.services.SecurityContextService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class SecurityContextTest extends JaxRsTestCase {
    @Override
    @SuppressWarnings("unchecked")
    protected Collection createRootResourceColl() {
        return Collections.singleton(SecurityContextService.class);
    }

    /**
     * @param authenticator
     * @return true, if it could be set, or false if not.
     */
    private boolean setAuthroizator(Authenticator authenticator) {
        ServerWrapper serverWrapper = getServerWrapper();
        if (serverWrapper instanceof RestletServerWrapper) {
            RestletServerWrapper restletServerWrapper = ((RestletServerWrapper) serverWrapper);
            restletServerWrapper.setAuthorizator(authenticator);
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldStartServerInSetUp() {
        return false;
    }

    public void testAllowAll() throws Exception {
        if (!setAuthroizator(AllowAllAuthenticator.getInstance()))
            return;
        startServer();
        Response response;
        response = accessServer(SecurityContextService.class, Method.GET);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        response = accessServer(SecurityContextService.class, Method.POST);
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        Reference expecretLocation = createReference(
                SecurityContextService.class, null);
        assertTrue("The location must start with " + expecretLocation
                + "; it is " + response.getLocationRef(), response
                .getLocationRef().toString().startsWith(
                        expecretLocation.toString()));
    }

    public void testForbidAll() throws Exception {
        if (!setAuthroizator(ForbidAllAuthenticator.getInstance()))
            return;
        startServer();
        Response response;
        response = accessServer(SecurityContextService.class, Method.GET);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        response = accessServer(SecurityContextService.class, Method.POST);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    public void testNoRoles() throws Exception {
        // FIXME dieser Test ist noch nicht fertig
        Authenticator exampleAuthorizator = new Authenticator() {

            /**
             * @returns true, if the first char of the password is 'a'
             */
            public boolean checkSecret(String identifier, char[] secret) {
                if (secret[0] == 'a')
                    return true;
                return false;
            }

            /**
             * @return true, if the role name and the username starts with the
             *         same char.
             */
            public boolean isUserInRole(Principal principal, String role) {
                if (role.charAt(0) == principal.getName().charAt(0))
                    return true;
                return false;
            }
        };
        if (!setAuthroizator(exampleAuthorizator))
            return;
        startServer();
        ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "fsdf", "xyz");
        Response response = accessServer(SecurityContextService.class, null,
                Method.GET, null, challengeResponse);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
                "fsdf", "baj");
        response = accessServer(SecurityContextService.class, null, Method.GET,
                null, challengeResponse);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
                "fsdf", "baj");
        response = accessServer(SecurityContextService.class, null, Method.GET,
                null, challengeResponse);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }
}