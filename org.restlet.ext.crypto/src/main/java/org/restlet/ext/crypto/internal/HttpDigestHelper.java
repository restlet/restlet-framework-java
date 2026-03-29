/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import java.util.Base64;
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
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Implements the HTTP DIGEST authentication.
 *
 * @author Jerome Louvel
 */
public class HttpDigestHelper extends AuthenticatorHelper {

    private static final String ALGORITHM = "algorithm";
    private static final String CNONCE = "cnonce";
    private static final String DOMAIN = "domain";
    private static final String NC = "nc";
    private static final String NONCE = "nonce";
    private static final String OPAQUE = "opaque";
    private static final String QUALITY_OPTION = "qop";
    private static final String REALM = "realm";
    private static final String RESPONSE = "response";
    private static final String STALE = "stale";
    private static final String URI = "uri";
    private static final String USERNAME = "username";

    /**
     * Checks whether the specified nonce is valid with respect to the specified secretKey and
     * further confirms that the nonce was generated less than lifespanMillis milliseconds ago
     *
     * @param nonce The nonce value.
     * @param secretKey The same secret value that was inserted into the nonce when it was generated
     * @param lifespan The nonce lifespan in milliseconds.
     * @return True if the nonce was generated less than lifespan milliseconds ago, false otherwise.
     * @throws Exception If the nonce does not match the specified secretKey, or if it can't be
     *     parsed
     */
    public static boolean isNonceValid(String nonce, String secretKey, long lifespan)
            throws Exception {
        try {
            String decodedNonce = new String(Base64.getDecoder().decode(nonce));
            long nonceTimeMS = Long.parseLong(decodedNonce.substring(0, decodedNonce.indexOf(':')));

            if (decodedNonce.equals(
                    nonceTimeMS + ":" + DigestUtils.toMd5(nonceTimeMS + ":" + secretKey))) {
                // Valid with regard to the secretKey, now check lifespan
                return lifespan > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new Exception("Error detected parsing nonce: " + e);
        }

        throw new Exception("The nonce does not match secretKey");
    }

    /** Constructor. */
    public HttpDigestHelper() {
        super(ChallengeScheme.HTTP_DIGEST, true, true);
    }

    @Override
    public void formatRequest(
            ChallengeWriter cw,
            ChallengeRequest challenge,
            Response response,
            Series<Header> httpHeaders) {

        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter(REALM, challenge.getRealm());
        } else {
            getLogger()
                    .warning(
                            "The realm directive is required for all authentication schemes that issue a challenge.");
        }

        if (!challenge.getDomainRefs().isEmpty()) {
            appendDomainRefs(cw, challenge);
        }

        if (challenge.getServerNonce() != null) {
            cw.appendQuotedChallengeParameter(NONCE, challenge.getServerNonce());
        }

        if (challenge.getOpaque() != null) {
            cw.appendQuotedChallengeParameter(OPAQUE, challenge.getOpaque());
        }

        if (challenge.isStale()) {
            cw.appendChallengeParameter(STALE, "true");
        }

        if (challenge.getDigestAlgorithm() != null) {
            cw.appendChallengeParameter(ALGORITHM, challenge.getDigestAlgorithm());
        }

        if (!challenge.getQualityOptions().isEmpty()) {
            appendQualityOptions(cw, challenge);
        }

        appendChallengeParameters(cw, challenge.getParameters());
    }

    private static void appendQualityOptions(
            final ChallengeWriter cw, final ChallengeRequest challenge) {
        cw.append(", " + QUALITY_OPTION + "=\"");

        for (int i = 0; i < challenge.getQualityOptions().size(); i++) {
            if (i > 0) {
                cw.append(',');
            }

            cw.appendToken(challenge.getQualityOptions().get(i));
        }

        cw.append('"');
    }

    private static void appendDomainRefs(
            final ChallengeWriter cw, final ChallengeRequest challenge) {
        cw.append(", " + DOMAIN + "=\"");

        for (int i = 0; i < challenge.getDomainRefs().size(); i++) {
            if (i > 0) {
                cw.append(' ');
            }

            cw.append(challenge.getDomainRefs().get(i).toString());
        }

        cw.append('"');
    }

    @Override
    public void formatResponse(
            ChallengeWriter cw,
            ChallengeResponse challenge,
            Request request,
            Series<Header> httpHeaders) {

        if (challenge.getIdentifier() != null) {
            cw.appendQuotedChallengeParameter(USERNAME, challenge.getIdentifier());
        }

        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter(REALM, challenge.getRealm());
        }

        if (challenge.getServerNonce() != null) {
            cw.appendQuotedChallengeParameter(NONCE, challenge.getServerNonce());
        }

        if (challenge.getDigestRef() != null) {
            challenge.setDigestRef(new Reference(request.getResourceRef().getPath()));
            cw.appendQuotedChallengeParameter(URI, challenge.getDigestRef().toString());
        }

        char[] responseDigest = formatResponseDigest(challenge, request);

        if (responseDigest != null) {
            cw.appendQuotedChallengeParameter(RESPONSE, new String(responseDigest));
        }

        if ((challenge.getDigestAlgorithm() != null)
                && !Digest.ALGORITHM_MD5.equals(challenge.getDigestAlgorithm())) {
            cw.appendChallengeParameter(ALGORITHM, challenge.getDigestAlgorithm());
        }

        if (challenge.getClientNonce() != null) {
            cw.appendQuotedChallengeParameter(CNONCE, challenge.getClientNonce());
        }

        if (challenge.getOpaque() != null) {
            cw.appendQuotedChallengeParameter(OPAQUE, challenge.getOpaque());
        }

        if (challenge.getQuality() != null) {
            cw.appendChallengeParameter(QUALITY_OPTION, challenge.getQuality());
        }

        if ((challenge.getQuality() != null) && (challenge.getServerNonceCount() > 0)) {
            cw.appendChallengeParameter(NC, challenge.getServerNonceCountAsHex());
        }

        appendChallengeParameters(cw, challenge.getParameters());
    }

    private static void appendChallengeParameters(
            final ChallengeWriter cw, final Series<Parameter> challenge) {
        for (Parameter param : challenge) {
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
     * @param challengeResponse The challenge response.
     * @param request The request if available.
     * @return The formatted secret of a challenge response.
     */
    public char[] formatResponseDigest(ChallengeResponse challengeResponse, Request request) {
        String a1 = null;

        if (Digest.ALGORITHM_HTTP_DIGEST.equals(challengeResponse.getSecretAlgorithm())) {
            a1 = new String(challengeResponse.getSecret());
        } else {
            if (!AuthenticatorUtils.anyNull(
                    challengeResponse.getIdentifier(),
                    challengeResponse.getSecret(),
                    challengeResponse.getRealm())) {
                a1 =
                        DigestUtils.toHttpDigest(
                                challengeResponse.getIdentifier(),
                                challengeResponse.getSecret(),
                                challengeResponse.getRealm());
            }
        }

        if (a1 != null
                && !AuthenticatorUtils.anyNull(
                        request.getMethod(), challengeResponse.getDigestRef())) {
            StringBuilder sb =
                    new StringBuilder()
                            .append(a1)
                            .append(':')
                            .append(challengeResponse.getServerNonce());

            if (!AuthenticatorUtils.anyNull(
                    challengeResponse.getQuality(),
                    challengeResponse.getClientNonce(),
                    challengeResponse.getServerNonceCount())) {
                sb.append(':')
                        .append(
                                AuthenticatorUtils.formatNonceCount(
                                        challengeResponse.getServerNonceCount()))
                        .append(':')
                        .append(challengeResponse.getClientNonce())
                        .append(':')
                        .append(challengeResponse.getQuality());
            }

            String a2 =
                    DigestUtils.toMd5(
                            request.getMethod().toString()
                                    + ":"
                                    + challengeResponse.getDigestRef().toString());
            sb.append(':').append(a2);

            return DigestUtils.toMd5(sb.toString()).toCharArray();
        }

        return null;
    }

    @Override
    public void parseRequest(
            ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
        if (challenge.getRawValue() == null) {
            return;
        }

        HeaderReader<Object> hr = new HeaderReader<>(challenge.getRawValue());

        try {
            Parameter param = hr.readParameter();

            while (param != null) {
                updateChallenge(challenge, param);

                if (hr.skipValueSeparator()) {
                    param = hr.readParameter();
                } else {
                    param = null;
                }
            }
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the challenge request header parameter",
                            e);
        }
    }

    private static void updateChallenge(final ChallengeRequest challenge, final Parameter param) {
        try {
            if (REALM.equals(param.getName())) {
                challenge.setRealm(param.getValue());
            } else if (DOMAIN.equals(param.getName())) {
                challenge.getDomainRefs().add(new Reference(param.getValue()));
            } else if (NONCE.equals(param.getName())) {
                challenge.setServerNonce(param.getValue());
            } else if (OPAQUE.equals(param.getName())) {
                challenge.setOpaque(param.getValue());
            } else if (STALE.equals(param.getName())) {
                challenge.setStale(Boolean.parseBoolean(param.getValue()));
            } else if (ALGORITHM.equals(param.getName())) {
                challenge.setDigestAlgorithm(param.getValue());
            } else if (QUALITY_OPTION.equals(param.getName())) {
                challenge.getQualityOptions().add(param.getValue());
            } else {
                challenge.getParameters().add(param);
            }
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the challenge request header parameter",
                            e);
        }
    }

    @Override
    public void parseResponse(
            ChallengeResponse challenge, Request request, Series<Header> httpHeaders) {
        if (challenge.getRawValue() != null) {
            HeaderReader<Object> hr = new HeaderReader<>(challenge.getRawValue());

            try {
                Parameter param = hr.readParameter();

                while (param != null) {
                    updateChallenge(challenge, param);
                    if (hr.skipValueSeparator()) {
                        param = hr.readParameter();
                    } else {
                        param = null;
                    }
                }
            } catch (Exception e) {
                Context.getCurrentLogger()
                        .log(
                                Level.WARNING,
                                "Unable to parse the challenge response header parameter",
                                e);
            }
        }
    }

    private static void updateChallenge(final ChallengeResponse challenge, final Parameter param) {
        try {
            if (USERNAME.equals(param.getName())) {
                challenge.setIdentifier(param.getValue());
            } else if (REALM.equals(param.getName())) {
                challenge.setRealm(param.getValue());
            } else if (NONCE.equals(param.getName())) {
                challenge.setServerNonce(param.getValue());
            } else if (URI.equals(param.getName())) {
                challenge.setDigestRef(new Reference(param.getValue()));
            } else if (RESPONSE.equals(param.getName())) {
                challenge.setSecret(param.getValue());
            } else if (ALGORITHM.equals(param.getName())) {
                challenge.setDigestAlgorithm(param.getValue());
            } else if (CNONCE.equals(param.getName())) {
                challenge.setClientNonce(param.getValue());
            } else if (OPAQUE.equals(param.getName())) {
                challenge.setOpaque(param.getValue());
            } else if (QUALITY_OPTION.equals(param.getName())) {
                challenge.setQuality(param.getValue());
            } else if (NC.equals(param.getName())) {
                challenge.setServerNonceCount(Integer.valueOf(param.getValue(), 16));
            } else {
                challenge.getParameters().add(param);
            }
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "Unable to parse the challenge response header parameter",
                            e);
        }
    }
}
