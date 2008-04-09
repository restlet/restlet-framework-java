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
import net.oauth.OAuthMessage;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * An OAuth Access Token.
 * 
 * @author Adam Rosien
 */
public class AccessTokenResource extends Resource {

    private OAuthProvider provider;

    private String realm;

    /**
     * Constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AccessTokenResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        provider = (OAuthProvider) context.getAttributes()
                .get("oauth_provider");
        realm = (String) context.getAttributes().get("realm");

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) {
        handle();
        return getResponse().getEntity();
    }

    @Override
    public void acceptRepresentation(Representation entity) {
        handle();
    }

    private void handle() {
        OAuthMessage requestMessage = OAuthHelper.getMessage(getRequest(),
                getLogger());
        OAuthAccessor accessor = provider.getAccessor(requestMessage);
        ChallengeRequest challengeRequest = new ChallengeRequest(
                OAuthGuard.SCHEME, realm);

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
            requestMessage.validateSignature(accessor);
        } catch (Exception e1) {
            getResponse().setChallengeRequest(challengeRequest);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Invalid signature");
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

        provider.generateAccessToken(accessor);

        try {
            getResponse().setEntity(
                    new StringRepresentation(OAuth.formEncode(OAuth.newList(
                            "oauth_token", accessor.accessToken,
                            "oauth_token_secret", accessor.tokenSecret))));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
