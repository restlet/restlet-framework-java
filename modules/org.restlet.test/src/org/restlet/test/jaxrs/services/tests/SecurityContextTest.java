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

import javax.ws.rs.core.SecurityContext;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.AllowAllAuthenticator;
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.ForbidAllAuthenticator;
import org.restlet.test.jaxrs.server.RestletServerWrapper;
import org.restlet.test.jaxrs.server.ServerWrapper;
import org.restlet.test.jaxrs.services.SecurityContextService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class SecurityContextTest extends JaxRsTestCase {
    private static final Class<SecurityContextService> SEC_CONT_SERV = SecurityContextService.class;

    @Override
    protected Class<?> getRootResourceClass() {
        return SEC_CONT_SERV;
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

    /**
     * @param authenticator
     * @throws Exception
     */
    private boolean startServer(Authenticator authenticator) throws Exception {
        if (!setAuthroizator(authenticator))
            return false;
        startServer();
        return true;
    }

    /**
     * Allow access, but forbid all rules
     * 
     * @throws Exception
     */
    public void test2() throws Exception {
        if (!startServer(new Authenticator() {
            public boolean checkSecret(String identifier, char[] secret) {
                return true;
            }

            public boolean isUserInRole(Principal principal, String role) {
                return false;
            }
        }))
            return;
        Response response = get();
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        response = post(new Form().getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        response = post(new Form("abc=def").getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    public void testAllowAll() throws Exception {
        if (!startServer(AllowAllAuthenticator.getInstance()))
            return;
        Response response = get();
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        response = post(new Form("abc=def").getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        Reference expecretLocation = createReference(SEC_CONT_SERV, null);
        assertTrue("The location must start with " + expecretLocation
                + "; it is " + response.getLocationRef(), response
                .getLocationRef().toString().startsWith(
                        expecretLocation.toString()));
    }

    public void testAuthenticationSchemeBasic() throws Exception {
        if (!startServer(AllowAllAuthenticator.getInstance()))
            return;
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "u", "p");
        Response response = get("authenticationScheme", cr);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String entity = response.getEntity().getText();
        assertEquals(SecurityContext.BASIC_AUTH, entity);
    }

    // TODO waiting for Digest Auth is running
    public void _testAuthenticationSchemeDigest() throws Exception {
        if (!setAuthroizator(AllowAllAuthenticator.getInstance()))
            return;
        startServer(ChallengeScheme.HTTP_DIGEST);
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_DIGEST, "u", "p");
        Response response = get("authenticationScheme", cr);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String entity = response.getEntity().getText();
        assertEquals(SecurityContext.DIGEST_AUTH, entity);
    }

    public void testForbidAll() throws Exception {
        if (!startServer(ForbidAllAuthenticator.getInstance()))
            return;
        Response response = get();
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        response = post(new Form("abc=def").getWebRepresentation());
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    /**
     * @throws Exception
     */
    public void testNoRoles() throws Exception {
        Authenticator exampleAuthorizator = new Authenticator() {
            /**
             * @returns true, if the first char of the password is 'a'
             * @see Authenticator#checkSecret(String, char[])
             */
            public boolean checkSecret(String identifier, char[] secret) {
                if (secret[0] == 'a')
                    return true;
                return false;
            }

            /**
             * @return true, if the role name and the username starts with the
             *         same char.
             * @see Authenticator#isUserInRole(String)
             */
            public boolean isUserInRole(Principal principal, String role) {
                if (principal == null)
                    throw new IllegalArgumentException("No principal given");
                if (role == null)
                    throw new IllegalArgumentException("No role given");
                if (role.charAt(0) == principal.getName().charAt(0))
                    return true;
                return false;
            }
        };
        if (!startServer(exampleAuthorizator))
            return;
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "fsdf", "xyz");
        Response response = get(null, cr);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "fsdf", "baj");
        response = get(null, cr);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "fsdf", "abj");
        response = get(null, cr);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());

        cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "bsdf", "abaj");
        response = get(null, cr);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "fsdf", "axa2");
        response = post(null, new Form("abc=def").getWebRepresentation(), cr);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        response = accessServer(Method.PUT, SEC_CONT_SERV, null, null, cr);
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());
    }

    // hope that Restlet Request.isConfidential(); works right with HTTPS

    public void testSecure() throws Exception {
        if (!startServer(AllowAllAuthenticator.getInstance()))
            return;
        Response response = get("secure");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testUserPrincipal() throws Exception {
        if (!startServer(AllowAllAuthenticator.getInstance()))
            return;
        Response response = get("userPrincipal");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String entity = response.getEntity().getText();
        assertEquals("-", entity);

        response = get("userPrincipal", new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "abc", "def"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        entity = response.getEntity().getText();
        assertEquals("abc", entity);

        response = get("userPrincipal", new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "asdfsdfbc", "def"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        entity = response.getEntity().getText();
        assertEquals("asdfsdfbc", entity);
    }
}