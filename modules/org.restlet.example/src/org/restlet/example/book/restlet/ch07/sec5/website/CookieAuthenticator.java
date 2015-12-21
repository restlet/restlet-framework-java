/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch07.sec5.website;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

public class CookieAuthenticator extends ChallengeAuthenticator {

    public CookieAuthenticator(Context context, boolean optional, String realm) {
        super(context, optional, ChallengeScheme.HTTP_COOKIE, realm);
    }

    public CookieAuthenticator(Context context, boolean optional, String realm,
            Verifier verifier) {
        super(context, optional, ChallengeScheme.HTTP_COOKIE, realm, verifier);
    }

    public CookieAuthenticator(Context context, String realm) {
        super(context, ChallengeScheme.HTTP_COOKIE, realm);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        Cookie cookie = request.getCookies().getFirst("Credentials");

        if (cookie != null) {
            // Extract the challenge response from the cookie
            String[] credentials = cookie.getValue().split("=");

            if (credentials.length == 2) {
                String identifier = credentials[0];
                String secret = credentials[1];
                request.setChallengeResponse(new ChallengeResponse(
                        ChallengeScheme.HTTP_COOKIE, identifier, secret));
            }
        } else if (Method.POST.equals(request.getMethod())
                && request.getResourceRef().getQueryAsForm().getFirst("login") != null) {
            // Intercepting a login form
            Form credentials = new Form(request.getEntity());
            String identifier = credentials.getFirstValue("identifier");
            String secret = credentials.getFirstValue("secret");
            request.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_COOKIE, identifier, secret));

            // Continue call processing to return the target representation if
            // authentication is successful or a new login page
            request.setMethod(Method.GET);
        }

        return super.beforeHandle(request, response);
    }

    @Override
    public void challenge(Response response, boolean stale) {
        // Load the FreeMarker template
        Representation ftl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Login.ftl").get();

        // Wraps the bean with a FreeMarker representation
        response.setEntity(new TemplateRepresentation(ftl, response
                .getRequest().getResourceRef(), MediaType.TEXT_HTML));
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        super.afterHandle(request, response);
        Cookie cookie = request.getCookies().getFirst("Credentials");

        if (request.getClientInfo().isAuthenticated() && (cookie == null)) {
            String identifier = request.getChallengeResponse().getIdentifier();
            String secret = new String(request.getChallengeResponse()
                    .getSecret());
            CookieSetting cookieSetting = new CookieSetting("Credentials",
                    identifier + "=" + secret);
            cookieSetting.setAccessRestricted(true);
            cookieSetting.setPath("/");
            cookieSetting.setComment("Unsecured cookie based authentication");
            cookieSetting.setMaxAge(30);
            response.getCookieSettings().add(cookieSetting);
        }
    }

}
