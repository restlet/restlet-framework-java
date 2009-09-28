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

import java.io.IOException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.SimpleOAuthValidator;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * An OAuth Access Token.
 * 
 * @author Adam Rosien
 */
public class AccessTokenResource extends Resource {

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
    public AccessTokenResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        this.provider = (OAuthProvider) context.getAttributes().get(
                "oauth_provider");
        this.realm = (String) context.getAttributes().get("realm");

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        handle();
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /**
     * Handles both GET and POST requests.
     * 
     * @throws ResourceException
     */
    private void handle() throws ResourceException {
        final OAuthMessage requestMessage = OAuthHelper
                .getMessage(getRequest());
        final OAuthAccessor accessor = this.provider
                .getAccessor(requestMessage);
        final ChallengeRequest challengeRequest = new ChallengeRequest(
                ChallengeScheme.HTTP_OAUTH, this.realm);

        if (accessor == null) {
            getResponse().setChallengeRequest(challengeRequest);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Invalid Consumer Key");
            challengeRequest.getParameters().add("oauth_problem",
                    "consumer_key_unknown");
            return;
        }

        // verify the signature
        try {
            requestMessage
                    .validateMessage(accessor, new SimpleOAuthValidator());
        } catch (Exception e1) {
            getResponse().setChallengeRequest(challengeRequest);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e1);
            challengeRequest.getParameters().add("oauth_problem",
                    "signature_invalid");
            return;
        }

        // make sure token is authorized
        if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
            getResponse().setChallengeRequest(challengeRequest);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Invalid / expired Token");
            challengeRequest.getParameters().add("oauth_problem",
                    "token_rejected");
            return;
        }

        this.provider.generateAccessToken(accessor);

        try {
            getResponse().setEntity(
                    new StringRepresentation(OAuth.formEncode(OAuth.newList(
                            "oauth_token", accessor.accessToken,
                            "oauth_token_secret", accessor.tokenSecret))));
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        handle();
        return getResponse().getEntity();
    }
}
