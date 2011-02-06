/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.http.security;

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
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.security.AuthenticatorHelper;
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
    public void formatRawRequest(ChallengeWriter cw,
            ChallengeRequest challenge, Response response,
            Series<Parameter> httpHeaders) throws IOException {
        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter("realm", challenge.getRealm());
        }
    }

    @Override
    public void formatRawResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
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
            Series<Parameter> httpHeaders) {
        if (challenge.getRawValue() != null) {
            HeaderReader<Object> hr = new HeaderReader<Object>(challenge
                    .getRawValue());

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
                        Context
                                .getCurrentLogger()
                                .log(
                                        Level.WARNING,
                                        "Unable to parse the challenge request header parameter",
                                        e);
                    }
                }
            } catch (Exception e) {
                Context
                        .getCurrentLogger()
                        .log(
                                Level.WARNING,
                                "Unable to parse the challenge request header parameter",
                                e);
            }
        }
    }

    @Override
    public void parseResponse(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        try {
            byte[] credentialsEncoded = Base64.decode(challenge.getRawValue());

            if (credentialsEncoded == null) {
                getLogger()
                        .warning(
                                "Cannot decode credentials: "
                                        + challenge.getRawValue());
            }

            String credentials = new String(credentialsEncoded, "ISO-8859-1");
            int separator = credentials.indexOf(':');

            if (separator == -1) {
                // Log the blocking
                getLogger().warning(
                        "Invalid credentials given by client with IP: "
                                + ((request != null) ? request.getClientInfo()
                                        .getAddress() : "?"));
            } else {
                challenge.setIdentifier(credentials.substring(0, separator));
                challenge.setSecret(credentials.substring(separator + 1));
            }
        } catch (UnsupportedEncodingException e) {
            getLogger().log(Level.WARNING, "Unsupported encoding error", e);
        }
    }

}
