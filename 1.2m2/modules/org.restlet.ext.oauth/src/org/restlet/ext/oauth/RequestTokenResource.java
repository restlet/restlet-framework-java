/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.oauth;

import java.io.IOException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.SimpleOAuthValidator;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * Get a OAuth request token.
 * 
 * @author Adam Rosien
 */
public class RequestTokenResource extends Resource {

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
    public RequestTokenResource(Context context, Request request,
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
        /*
         * This is stolen and modified from RequestTokenServlet in the OAuth
         * Java source.
         */
        final OAuthMessage requestMessage = OAuthHelper
                .getMessage(getRequest());
        final OAuthConsumer consumer = this.provider
                .getConsumer(requestMessage);
        final ChallengeRequest challengeRequest = new ChallengeRequest(
                ChallengeScheme.HTTP_OAUTH, this.realm);

        if (consumer == null) {
            getResponse().setChallengeRequest(challengeRequest);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Invalid Consumer Key");
            challengeRequest.getParameters().add("oauth_problem",
                    "consumer_key_unknown");
            return;
        }

        final OAuthAccessor accessor = new OAuthAccessor(consumer);

        // verify the signature
        try {
            requestMessage
                    .validateMessage(accessor, new SimpleOAuthValidator());
        } catch (OAuthProblemException oape) {
            getResponse().setChallengeRequest(challengeRequest);
            // TODO: Use OAuthServlet mapping from problem to status.
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, oape);
            challengeRequest.getParameters().add("oauth_problem",
                    "signature_invalid");
            return;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

        // generate request_token and secret
        this.provider.generateRequestToken(accessor);

        try {
            getResponse().setEntity(
                    new StringRepresentation(OAuth.formEncode(OAuth.newList(
                            "oauth_token", accessor.requestToken,
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
