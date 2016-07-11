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

package org.restlet.ext.crypto.internal;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Digest;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Implements the HTTP DIGEST authentication.
 * 
 * @author Jerome Louvel
 */
public class HttpDigestHelper extends AuthenticatorHelper {

    /**
     * Checks whether the specified nonce is valid with respect to the specified
     * secretKey, and further confirms that the nonce was generated less than
     * lifespanMillis milliseconds ago
     * 
     * @param nonce
     *            The nonce value.
     * @param secretKey
     *            The same secret value that was inserted into the nonce when it
     *            was generated
     * @param lifespan
     *            The nonce lifespan in milliseconds.
     * @return True if the nonce was generated less than lifespan milliseconds
     *         ago, false otherwise.
     * @throws Exception
     *             If the nonce does not match the specified secretKey, or if it
     *             can't be parsed
     */
    public static boolean isNonceValid(String nonce, String secretKey,
            long lifespan) throws Exception {
        try {
            String decodedNonce = new String(Base64.decode(nonce));
            long nonceTimeMS = Long.parseLong(decodedNonce.substring(0,
                    decodedNonce.indexOf(':')));

            if (decodedNonce.equals(nonceTimeMS + ":"
                    + DigestUtils.toMd5(nonceTimeMS + ":" + secretKey))) {
                // Valid with regard to the secretKey, now check lifespan
                return lifespan > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new Exception("Error detected parsing nonce: " + e);
        }

        throw new Exception("The nonce does not match secretKey");
    }

    /**
     * Constructor.
     */
    public HttpDigestHelper() {
        super(ChallengeScheme.HTTP_DIGEST, true, true);
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

        if (!challenge.getDomainRefs().isEmpty()) {
            cw.append(", domain=\"");

            for (int i = 0; i < challenge.getDomainRefs().size(); i++) {
                if (i > 0) {
                    cw.append(' ');
                }

                cw.append(challenge.getDomainRefs().get(i).toString());
            }

            cw.append('"');
        }

        if (challenge.getServerNonce() != null) {
            cw.appendQuotedChallengeParameter("nonce",
                    challenge.getServerNonce());
        }

        if (challenge.getOpaque() != null) {
            cw.appendQuotedChallengeParameter("opaque", challenge.getOpaque());
        }

        if (challenge.isStale()) {
            cw.appendChallengeParameter("stale", "true");
        }

        if (challenge.getDigestAlgorithm() != null) {
            cw.appendChallengeParameter("algorithm",
                    challenge.getDigestAlgorithm());
        }

        if (!challenge.getQualityOptions().isEmpty()) {
            cw.append(", qop=\"");

            for (int i = 0; i < challenge.getQualityOptions().size(); i++) {
                if (i > 0) {
                    cw.append(',');
                }

                cw.appendToken(challenge.getQualityOptions().get(i).toString());
            }

            cw.append('"');
        }

        for (Parameter param : challenge.getParameters()) {
            if (HeaderUtils.isToken(param.getValue())) {
                cw.appendChallengeParameter(param);
            } else {
                cw.appendQuotedChallengeParameter(param);
            }
        }
    }

    @Override
    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {

        if (challenge.getIdentifier() != null) {
            cw.appendQuotedChallengeParameter("username",
                    challenge.getIdentifier());
        }

        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter("realm", challenge.getRealm());
        }

        if (challenge.getServerNonce() != null) {
            cw.appendQuotedChallengeParameter("nonce",
                    challenge.getServerNonce());
        }

        if (challenge.getDigestRef() != null) {
            challenge.setDigestRef(new Reference(request.getResourceRef()
                    .getPath()));
            cw.appendQuotedChallengeParameter("uri", challenge.getDigestRef()
                    .toString());
        }

        char[] responseDigest = formatResponseDigest(challenge, request);

        if (responseDigest != null) {
            cw.appendQuotedChallengeParameter("response", new String(
                    responseDigest));
        }

        if ((challenge.getDigestAlgorithm() != null)
                && !Digest.ALGORITHM_MD5.equals(challenge.getDigestAlgorithm())) {
            cw.appendChallengeParameter("algorithm",
                    challenge.getDigestAlgorithm());
        }

        if (challenge.getClientNonce() != null) {
            cw.appendQuotedChallengeParameter("cnonce",
                    challenge.getClientNonce());
        }

        if (challenge.getOpaque() != null) {
            cw.appendQuotedChallengeParameter("opaque", challenge.getOpaque());
        }

        if (challenge.getQuality() != null) {
            cw.appendChallengeParameter("qop", challenge.getQuality());
        }

        if ((challenge.getQuality() != null)
                && (challenge.getServerNounceCount() > 0)) {
            cw.appendChallengeParameter("nc",
                    challenge.getServerNounceCountAsHex());
        }

        for (Parameter param : challenge.getParameters()) {
            if (HeaderUtils.isToken(param.getValue())) {
                cw.appendChallengeParameter(param);
            } else {
                cw.appendQuotedChallengeParameter(param);
            }
        }
    }

    /**
     * Formats the response digest.
     * 
     * @param challengeResponse
     *            The challenge response.
     * @param request
     *            The request if available.
     * @return The formatted secret of a challenge response.
     */
    public char[] formatResponseDigest(ChallengeResponse challengeResponse,
            Request request) {
        String a1 = null;

        if (!Digest.ALGORITHM_HTTP_DIGEST.equals(challengeResponse
                .getSecretAlgorithm())) {
            if (!AuthenticatorUtils
                    .anyNull(challengeResponse.getIdentifier(),
                            challengeResponse.getSecret(),
                            challengeResponse.getRealm())) {
                a1 = DigestUtils.toHttpDigest(
                        challengeResponse.getIdentifier(),
                        challengeResponse.getSecret(),
                        challengeResponse.getRealm());
            }
        } else {
            a1 = new String(challengeResponse.getSecret());
        }

        if (a1 != null
                && !AuthenticatorUtils.anyNull(request.getMethod(),
                        challengeResponse.getDigestRef())) {
            String a2 = DigestUtils.toMd5(request.getMethod().toString() + ":"
                    + challengeResponse.getDigestRef().toString());
            StringBuilder sb = new StringBuilder().append(a1).append(':')
                    .append(challengeResponse.getServerNonce());

            if (!AuthenticatorUtils.anyNull(challengeResponse.getQuality(),
                    challengeResponse.getClientNonce(),
                    challengeResponse.getServerNounceCount())) {
                sb.append(':')
                        .append(AuthenticatorUtils
                                .formatNonceCount(challengeResponse
                                        .getServerNounceCount())).append(':')
                        .append(challengeResponse.getClientNonce()).append(':')
                        .append(challengeResponse.getQuality());
            }

            sb.append(':').append(a2);

            return DigestUtils.toMd5(sb.toString()).toCharArray();
        }

        return null;
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
                        } else if ("domain".equals(param.getName())) {
                            challenge.getDomainRefs().add(
                                    new Reference(param.getValue()));
                        } else if ("nonce".equals(param.getName())) {
                            challenge.setServerNonce(param.getValue());
                        } else if ("opaque".equals(param.getName())) {
                            challenge.setOpaque(param.getValue());
                        } else if ("stale".equals(param.getName())) {
                            challenge
                                    .setStale(Boolean.valueOf(param.getValue()));
                        } else if ("algorithm".equals(param.getName())) {
                            challenge.setDigestAlgorithm(param.getValue());
                        } else if ("qop".equals(param.getName())) {
                            // challenge.setDigestAlgorithm(param.getValue());
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
        if (challenge.getRawValue() != null) {
            HeaderReader<Object> hr = new HeaderReader<Object>(
                    challenge.getRawValue());

            try {
                Parameter param = hr.readParameter();

                while (param != null) {
                    try {
                        if ("username".equals(param.getName())) {
                            challenge.setIdentifier(param.getValue());
                        } else if ("realm".equals(param.getName())) {
                            challenge.setRealm(param.getValue());
                        } else if ("nonce".equals(param.getName())) {
                            challenge.setServerNonce(param.getValue());
                        } else if ("uri".equals(param.getName())) {
                            challenge.setDigestRef(new Reference(param
                                    .getValue()));
                        } else if ("response".equals(param.getName())) {
                            challenge.setSecret(param.getValue());
                        } else if ("algorithm".equals(param.getName())) {
                            challenge.setDigestAlgorithm(param.getValue());
                        } else if ("cnonce".equals(param.getName())) {
                            challenge.setClientNonce(param.getValue());
                        } else if ("opaque".equals(param.getName())) {
                            challenge.setOpaque(param.getValue());
                        } else if ("qop".equals(param.getName())) {
                            challenge.setQuality(param.getValue());
                        } else if ("nc".equals(param.getName())) {
                            challenge.setServerNounceCount(Integer.valueOf(
                                    param.getValue(), 16));
                        } else {
                            challenge.getParameters().add(param);
                        }
                    } catch (Throwable e) {
                        Context.getCurrentLogger()
                                .log(Level.WARNING,
                                        "Unable to parse the challenge request header parameter",
                                        e);
                    }
                    if (hr.skipValueSeparator()) {
                        param = hr.readParameter();
                    } else {
                        param = null;
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

}
