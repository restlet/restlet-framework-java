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

import java.io.IOException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

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
        final OAuthMessage requestMessage = OAuthHelper.getMessage(
                getRequest(), getLogger());
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
            requestMessage.validateSignature(accessor);
        } catch (final OAuthProblemException oape) {
            getResponse().setChallengeRequest(challengeRequest);
            // TODO: Use OAuthServlet mapping from problem to status.
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, oape);
            challengeRequest.getParameters().add("oauth_problem",
                    "signature_invalid");
            return;
        } catch (final Exception e) {
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
        } catch (final IOException e) {
            throw new ResourceException(e);
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        handle();
        return getResponse().getEntity();
    }
}
