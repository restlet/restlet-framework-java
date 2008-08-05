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

package com.noelios.restlet.authentication;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.util.Series;

import com.noelios.restlet.util.Base64;

/**
 * Implements the HTTP BASIC authentication.
 * 
 * @author Jerome Louvel
 */
public class HttpBasicHelper extends AuthenticationHelper {

    /**
     * Constructor.
     */
    public HttpBasicHelper() {
        super(ChallengeScheme.HTTP_BASIC, true, true);
    }

    @Override
    public void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        try {
            final String credentials = challenge.getIdentifier() + ':'
                    + new String(challenge.getSecret());
            sb.append(Base64.encode(credentials.getBytes("US-ASCII"), false));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unsupported encoding, unable to encode credentials");
        }
    }

    @Override
    public void parseResponse(ChallengeResponse cr, Request request,
            Logger logger) {
        try {
            final byte[] credentialsEncoded = Base64
                    .decode(cr.getCredentials());
            if (credentialsEncoded == null) {
                logger.warning("Cannot decode credentials: "
                        + cr.getCredentials());
            }

            final String credentials = new String(credentialsEncoded,
                    "US-ASCII");
            final int separator = credentials.indexOf(':');

            if (separator == -1) {
                // Log the blocking
                logger.warning("Invalid credentials given by client with IP: "
                        + ((request != null) ? request.getClientInfo()
                                .getAddress() : "?"));
            } else {
                cr.setIdentifier(credentials.substring(0, separator));
                cr.setSecret(credentials.substring(separator + 1));
            }
        } catch (final UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Unsupported encoding error", e);
        }
    }

}
