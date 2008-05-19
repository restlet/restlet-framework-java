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

package com.noelios.restlet.ext.oauth;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Handles user authorization of an OAuth request token.
 * 
 * @author Adam Rosien
 */
public abstract class AuthorizationResource extends Resource {

    private OAuthProvider provider;

    private String realm;

    /**
     * Constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AuthorizationResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        provider = (OAuthProvider) context.getAttributes()
                .get("oauth_provider");
        realm = (String) context.getAttributes().get("realm");

        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /**
     * Return a user-accessible page asking if the user wants to authorize the
     * client.
     */
    @Override
    public abstract Representation represent(Variant variant);

    @Override
    public void acceptRepresentation(Representation entity) {
        OAuthMessage requestMessage = OAuthHelper.getMessage(getRequest(),
                getLogger());
        OAuthAccessor accessor = provider.getAccessor(requestMessage);

        if (accessor == null) {
            ChallengeRequest challengeRequest = new ChallengeRequest(
                    OAuthGuard.SCHEME, realm);
            getResponse().setChallengeRequest(challengeRequest);
            // TODO: Use OAuthServlet mapping from problem to status.
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Invalid / expired Token");
            challengeRequest.getParameters().add("oauth_problem",
                    "token_rejected");
            return;
        }

        if (!isAuthorized()) {
            handleFailedAuthorization();
            return;
        }

        String userId = "user";

        // set userId in accessor and mark it as authorized
        provider.markAsAuthorized(accessor, userId);

        String callback = getRequest().getResourceRef().getQueryAsForm()
                .getFirstValue("oauth_callback");
        if (callback != null) {
            getResponse().setLocationRef(callback);
            getResponse().setStatus(Status.REDIRECTION_FOUND);
        } else {
            getResponse()
                    .setEntity(
                            "You have allowed authorization. Please close this browser window and click continue"
                                    + " in the client.", MediaType.TEXT_PLAIN);
        }
    }

    /**
     * Handle if the user has denied authorization of a client. By default it
     * sets the response entity to the message "You have DENIED authorization."
     * to the client.
     */
    protected void handleFailedAuthorization() {
        getResponse().setEntity("You have DENIED authorization.",
                MediaType.TEXT_PLAIN);
    }

    /**
     * Return true if the request has been authorized by the user, false
     * otherwise.
     * 
     * @return True if the request has been authorized by the user, false
     *         otherwise.
     */
    protected abstract boolean isAuthorized();

}
