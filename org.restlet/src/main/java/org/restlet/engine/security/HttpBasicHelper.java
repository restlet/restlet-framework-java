/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
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
import org.restlet.engine.io.IoUtils;
import org.restlet.util.Series;

/**
 * Implements the HTTP BASIC authentication.
 *
 * @author Jerome Louvel
 */
public class HttpBasicHelper extends AuthenticatorHelper {

    private static final String CHARSET = "charset";
    private static final String REALM = "realm";
    private static final String UTF_8 = "UTF-8";
    private static final String ISO_8859_1 = "ISO-8859-1";

    /** Constructor. */
    public HttpBasicHelper() {
        super(ChallengeScheme.HTTP_BASIC, true, true);
    }

    @Override
    public void formatRequest(
            ChallengeWriter cw,
            ChallengeRequest challenge,
            Response response,
            Series<Header> httpHeaders) {
        String realm = challenge.getRealm();
        String charset = challenge.getParameters().getFirstValue(CHARSET);

        if (realm != null) {
            cw.appendQuotedChallengeParameter(REALM, realm);
        } else {
            getLogger()
                    .warning(
                            "The realm directive is required for all authentication schemes that issue a challenge.");
        }

        if (charset != null) {
            if (UTF_8.equalsIgnoreCase(charset)) {
                cw.appendQuotedChallengeParameter(CHARSET, UTF_8);
            } else {
                getLogger().warning("The \"charset\" parameter must be \"UTF-8\" per RFC 7617.");
            }
        }
    }

    @Override
    public void formatResponse(
            ChallengeWriter cw,
            ChallengeResponse challenge,
            Request request,
            Series<Header> httpHeaders) {
        if (challenge == null) {
            throw new IllegalArgumentException(
                    "No challenge provided, unable to encode credentials");
        } else {
            String charset = getCharset(challenge);
            try {

                CharArrayWriter credentials = new CharArrayWriter();
                credentials.write(challenge.getIdentifier());
                credentials.write(":");
                credentials.write(challenge.getSecret());
                cw.append(
                        Base64.getEncoder()
                                .encodeToString(
                                        IoUtils.toByteArray(credentials.toCharArray(), charset)));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(
                        "Unsupported encoding, unable to encode credentials" + charset);
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Unexpected exception, unable to encode credentials", e);
            }
        }
    }

    @Override
    public void parseRequest(
            ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
        if (challenge.getRawValue() == null) {
            return;
        }

        HeaderReader<Object> hr = new HeaderReader<>(challenge.getRawValue());

        Parameter param = null;
        try {
            param = hr.readParameter();
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the challenge request header parameter",
                            e);
        }

        while (param != null) {
            try {
                if (REALM.equals(param.getName())) {
                    challenge.setRealm(param.getValue());
                } else {
                    challenge.getParameters().add(param);
                }

                if (hr.skipValueSeparator()) {
                    param = hr.readParameter();
                } else {
                    param = null; // end of header
                }
            } catch (IOException e) {
                Context.getCurrentLogger()
                        .log(
                                Level.WARNING,
                                "Unable to parse the challenge request header parameter",
                                e);
            }
        }
    }

    @Override
    public void parseResponse(
            ChallengeResponse challenge, Request request, Series<Header> httpHeaders) {
        if (challenge.getRawValue() == null) {
            getLogger().info("Cannot decode credentials: " + challenge.getRawValue());
            return;
        }

        try {
            String charset = getCharset(challenge);

            byte[] credentialsEncoded = Base64.getDecoder().decode(challenge.getRawValue());

            String credentials = new String(credentialsEncoded, charset);
            int separator = credentials.indexOf(':');

            if (separator == -1) {
                // Log the blocking
                getLogger()
                        .log(
                                Level.INFO,
                                "Invalid credentials given by client with IP: {0}",
                                request != null ? request.getClientInfo().getAddress() : "?");
            } else {
                challenge.setIdentifier(credentials.substring(0, separator));
                challenge.setSecret(credentials.substring(separator + 1));
            }
        } catch (UnsupportedEncodingException e) {
            getLogger().log(Level.INFO, "Unsupported HTTP Basic encoding error", e);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.INFO, "Unable to decode the HTTP Basic credential", e);
        }
    }

    /**
     * Returns the charset from the given ChallengeResponse. If the charset is not specified, or if
     * it is not UTF-8, then ISO-8859-1 is returned as per RFC 7617.
     *
     * @param challenge The challenge response.
     * @return The charset.
     */
    private String getCharset(final ChallengeResponse challenge) {
        String charset = challenge.getParameters().getFirstValue(CHARSET);

        if (charset == null) {
            charset = ISO_8859_1;
        } else if (UTF_8.equalsIgnoreCase(charset)) {
            charset = UTF_8;
        } else {
            getLogger()
                    .warning(
                            "The \"charset\" parameter must be \"UTF-8\" per RFC 7617. Using \"ISO-8859-1\" instead.");
            charset = ISO_8859_1;
        }
        return charset;
    }
}
