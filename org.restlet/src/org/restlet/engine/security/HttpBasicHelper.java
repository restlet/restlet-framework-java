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

package org.restlet.engine.security;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.util.Base64;
import org.restlet.util.Series;

/**
 * Implements the HTTP BASIC authentication.
 * 
 * @author Jerome Louvel
 */
public class HttpBasicHelper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public HttpBasicHelper() {
        super(ChallengeScheme.HTTP_BASIC, true, true);
    }

    @Override
    public void formatRequest(ChallengeWriter cw, ChallengeRequest challenge,
            Response response, Series<Header> httpHeaders) throws IOException {
        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter("realm", challenge.getRealm());
        } else {
            getLogger()
                    .warning(
                            "The realm directive is required for all authentication schemes that issue a challenge.");
        }
    }

    @Override
    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
        try {
            if (challenge == null) {
                throw new RuntimeException(
                        "No challenge provided, unable to encode credentials");
            } else {
                CharArrayWriter credentials = new CharArrayWriter();
                credentials.write(challenge.getIdentifier());
                credentials.write(":");
                credentials.write(challenge.getSecret());
                cw.append(Base64.encode(credentials.toCharArray(),
                        "ISO-8859-1", false));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unsupported encoding, unable to encode credentials");
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception, unable to encode credentials", e);
        }
    }

    @Override
    public void parseRequest(ChallengeRequest challenge, Response response,
            Series<Header> httpHeaders) {
        if (challenge.getRawValue() != null) {
            HeaderReader<Object> hr = new HeaderReader<Object>(
                    challenge.getRawValue());

            try {
                Parameter param = hr.readParameter();

                while (param != null) {
                    try {
                        if ("realm".equals(param.getName())) {
                            challenge.setRealm(param.getValue());
                        } else {
                            challenge.getParameters().add(param);
                        }

                        if (hr.skipValueSeparator()) {
                            param = hr.readParameter();
                        } else {
                            param = null;
                        }
                    } catch (Exception e) {
                        Context.getCurrentLogger()
                                .log(Level.WARNING,
                                        "Unable to parse the challenge request header parameter",
                                        e);
                    }
                }
            } catch (Exception e) {
                Context.getCurrentLogger()
                        .log(Level.WARNING,
                                "Unable to parse the challenge request header parameter",
                                e);
            }
        }
    }

    @Override
    public void parseResponse(ChallengeResponse challenge, Request request,
            Series<Header> httpHeaders) {
        try {
            byte[] credentialsEncoded = Base64.decode(challenge.getRawValue());

            if (credentialsEncoded == null) {
                getLogger()
                        .info("Cannot decode credentials: "
                                + challenge.getRawValue());
            }

            String credentials = new String(credentialsEncoded, "ISO-8859-1");
            int separator = credentials.indexOf(':');

            if (separator == -1) {
                // Log the blocking
                getLogger().info(
                        "Invalid credentials given by client with IP: "
                                + ((request != null) ? request.getClientInfo()
                                        .getAddress() : "?"));
            } else {
                challenge.setIdentifier(credentials.substring(0, separator));
                challenge.setSecret(credentials.substring(separator + 1));
            }
        } catch (UnsupportedEncodingException e) {
            getLogger().log(Level.INFO,
                    "Unsupported HTTP Basic encoding error", e);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.INFO,
                    "Unable to decode the HTTP Basic credential", e);
        }
    }

}
