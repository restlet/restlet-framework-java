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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.util.Series;

import com.noelios.restlet.authentication.AuthenticationHelper;

/**
 * Helper routines for OAuth.
 * 
 * @author Adam Rosien
 */
public class OAuthHelper extends AuthenticationHelper {

    /**
     * Extract the parts of the given request that are relevant to OAuth.
     * Parameters include OAuth Authorization headers and the usual request
     * parameters in the query string and/or form encoded body. The header
     * parameters come first, followed by the rest in the order they came from
     * request.getParameterMap().
     * 
     * @param request
     * @param logger
     *            The context's logger.
     * @return message
     */
    public static OAuthMessage getMessage(Request request, Logger logger) {
        final String URL = request.getResourceRef().toString();
        return new OAuthMessage(request.getMethod().getName(), URL, OAuthHelper
                .getParameters(request, logger));
    }

    /**
     * Translate request parameters into OAuth.Parameter objects.
     * 
     * @param request
     * @param logger
     *            The context's logger.
     * @return parameters
     */
    public static List<OAuth.Parameter> getParameters(Request request,
            Logger logger) {
        final Set<OAuth.Parameter> parameters = new HashSet<OAuth.Parameter>();

        // Authorization headers.
        final Form headers = (Form) request.getAttributes().get(
                "org.restlet.http.headers");
        for (final OAuth.Parameter parameter : OAuthMessage
                .decodeAuthorization(headers.getFirstValue("Authorization"))) {
            if (!parameter.getKey().equalsIgnoreCase("realm")) {
                parameters.add(parameter);
            }
        }

        // Query parameters.
        for (final org.restlet.data.Parameter p : request.getResourceRef()
                .getQueryAsForm()) {
            parameters.add(new OAuth.Parameter(p.getName(), p.getValue()));
        }

        // POST with x-www-urlencoded data
        if ((request.getMethod() == Method.POST)
                && (request.getEntity().getMediaType() == MediaType.APPLICATION_WWW_FORM)) {
            for (final org.restlet.data.Parameter p : request.getEntityAsForm()) {
                parameters.add(new OAuth.Parameter(p.getName(), p.getValue()));
            }
        }

        logger.fine("Got OAuth parameters " + parameters);

        return new ArrayList<OAuth.Parameter>(parameters);
    }

    /**
     * Constructor.
     */
    public OAuthHelper() {
        super(OAuthGuard.SCHEME, false, true);
    }

    @Override
    public void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        // TODO Auto-generated method stub

    }

    @Override
    public void formatParameters(StringBuilder sb,
            Series<Parameter> parameters, ChallengeRequest request) {
        for (final Parameter p : parameters) {
            sb.append(",");
            sb.append(p.getName());
            sb.append("=\"");
            sb.append(p.getValue());
            sb.append("\"");
        }
        super.formatParameters(sb, parameters, request);
    }

    @Override
    public void parseResponse(ChallengeResponse cr, Request request,
            Logger logger) {
        super.parseResponse(cr, request, logger);
    }

}
