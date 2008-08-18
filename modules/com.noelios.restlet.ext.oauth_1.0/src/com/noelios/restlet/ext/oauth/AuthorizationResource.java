/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.oauth;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
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

    private final OAuthProvider provider;

    private final String realm;

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    public AuthorizationResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        this.provider = (OAuthProvider) context.getAttributes().get(
                "oauth_provider");
        this.realm = (String) context.getAttributes().get("realm");

        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public void acceptRepresentation(Representation entity) {
        final OAuthMessage requestMessage = OAuthHelper
                .getMessage(getRequest());
        final OAuthAccessor accessor = this.provider
                .getAccessor(requestMessage);

        if (accessor == null) {
            final ChallengeRequest challengeRequest = new ChallengeRequest(
                    ChallengeScheme.HTTP_OAUTH, this.realm);
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

        final String userId = "user";

        // set userId in accessor and mark it as authorized
        this.provider.markAsAuthorized(accessor, userId);

        final String callback = getRequest().getResourceRef().getQueryAsForm()
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

    @Override
    public boolean allowPost() {
        return true;
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

    /**
     * Return a user-accessible page asking if the user wants to authorize the
     * client.
     */
    @Override
    public abstract Representation represent(Variant variant);

}
