/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Parameter;

/**
 * Unit tests for the credentials charset handling of {@link HttpBasicHelper} (RFC 7617 section
 * 2.1), without any network.
 */
class HttpBasicHelperTestCase {

    @BeforeEach
    void enableUtf8() {
        System.setProperty(HttpBasicHelper.UTF8_PROPERTY, "true");
    }

    @AfterEach
    void restoreDefault() {
        System.clearProperty(HttpBasicHelper.UTF8_PROPERTY);
    }

    private static final String LATIN1_LOGIN = "josé";
    private static final String LATIN1_PASSWORD = "pässwörd";
    private static final String UTF8_LOGIN = "zoë";

    /** The euro sign does not exist in ISO-8859-1. */
    private static final String UTF8_PASSWORD = "p€ss";

    private static String basicHeader(String login, String password, java.nio.charset.Charset cs) {
        byte[] bytes = (login + ":" + password).getBytes(cs);
        return "Basic " + Base64.getEncoder().encodeToString(bytes);
    }

    private static String base64Of(String login, String password, java.nio.charset.Charset cs) {
        return Base64.getEncoder().encodeToString((login + ":" + password).getBytes(cs));
    }

    // ---- Server side: decoding the Authorization header --------------------------------------

    @Test
    void parseResponse_asciiCredentials_decoded() {
        ChallengeResponse cr =
                AuthenticatorUtils.parseResponse(
                        new Request(Method.GET, "http://localhost/"),
                        basicHeader("user", "secret", StandardCharsets.UTF_8),
                        null);
        assertEquals("user", cr.getIdentifier());
        assertEquals("secret", String.valueOf(cr.getSecret()));
    }

    /** Backward compatibility: bytes that are not valid UTF-8 are still read as ISO-8859-1. */
    @Test
    void parseResponse_latin1Bytes_decodedAsIso88591() {
        ChallengeResponse cr =
                AuthenticatorUtils.parseResponse(
                        new Request(Method.GET, "http://localhost/"),
                        basicHeader(LATIN1_LOGIN, LATIN1_PASSWORD, StandardCharsets.ISO_8859_1),
                        null);
        assertEquals(LATIN1_LOGIN, cr.getIdentifier());
        assertEquals(LATIN1_PASSWORD, String.valueOf(cr.getSecret()));
    }

    /** Issue #1455: the server advertises UTF-8 but decodes ISO-8859-1. */
    @Test
    void parseResponse_utf8Bytes_decodedAsUtf8() {
        ChallengeResponse cr =
                AuthenticatorUtils.parseResponse(
                        new Request(Method.GET, "http://localhost/"),
                        basicHeader(UTF8_LOGIN, UTF8_PASSWORD, StandardCharsets.UTF_8),
                        null);
        assertEquals(UTF8_LOGIN, cr.getIdentifier());
        assertEquals(UTF8_PASSWORD, String.valueOf(cr.getSecret()));
    }

    /** The default is unchanged: without the opt-in, credentials are read as ISO-8859-1. */
    @Test
    void parseResponse_utf8Bytes_utf8Disabled_decodedAsIso88591() {
        System.clearProperty(HttpBasicHelper.UTF8_PROPERTY);
        ChallengeResponse cr =
                AuthenticatorUtils.parseResponse(
                        new Request(Method.GET, "http://localhost/"),
                        basicHeader("zoë", "pw", StandardCharsets.UTF_8),
                        null);
        assertEquals("zoÃ«", cr.getIdentifier());
    }

    @Test
    void formatResponse_challengeAdvertisedUtf8_utf8Disabled_encodesIso88591() {
        System.clearProperty(HttpBasicHelper.UTF8_PROPERTY);
        Response response = new Response(new Request(Method.GET, "http://localhost/"));
        ChallengeRequest challenge = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");
        challenge.getParameters().add(new Parameter("charset", "UTF-8"));
        response.getChallengeRequests().add(challenge);

        ChallengeResponse cr =
                new ChallengeResponse(challenge, response, "josé", "pässwörd".toCharArray());

        assertEquals(
                "Basic " + base64Of("josé", "pässwörd", StandardCharsets.ISO_8859_1),
                AuthenticatorUtils.formatResponse(cr, response.getRequest(), null));
    }

    // ---- Server side: advertising the charset in the challenge -------------------------------

    @Test
    void formatRequest_charsetParameter_advertisesUtf8() {
        ChallengeRequest challenge = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");
        challenge.getParameters().add(new Parameter("charset", "UTF-8"));

        assertEquals(
                "Basic realm=\"realm\", charset=\"UTF-8\"",
                AuthenticatorUtils.formatRequest(challenge, new Response(null), null));
    }

    @Test
    void formatRequest_noCharsetParameter_realmOnly() {
        ChallengeRequest challenge = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");

        assertEquals(
                "Basic realm=\"realm\"",
                AuthenticatorUtils.formatRequest(challenge, new Response(null), null));
    }

    // ---- Client side: encoding the Authorization header --------------------------------------

    @Test
    void formatResponse_default_encodesIso88591() {
        ChallengeResponse cr =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, LATIN1_LOGIN, LATIN1_PASSWORD);

        assertEquals(
                "Basic " + base64Of(LATIN1_LOGIN, LATIN1_PASSWORD, StandardCharsets.ISO_8859_1),
                AuthenticatorUtils.formatResponse(
                        cr, new Request(Method.GET, "http://localhost/"), null));
    }

    @Test
    void formatResponse_charsetParameter_encodesUtf8() {
        ChallengeResponse cr =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, UTF8_LOGIN, UTF8_PASSWORD);
        cr.getParameters().add(new Parameter("charset", "UTF-8"));

        assertEquals(
                "Basic " + base64Of(UTF8_LOGIN, UTF8_PASSWORD, StandardCharsets.UTF_8),
                AuthenticatorUtils.formatResponse(
                        cr, new Request(Method.GET, "http://localhost/"), null));
    }

    /**
     * Issue #1455: a client that answers a challenge advertising {@code charset="UTF-8"} should
     * encode its credentials in UTF-8 without any manual parameter.
     */
    @Test
    void formatResponse_challengeAdvertisedUtf8_encodesUtf8() {
        Response response = new Response(new Request(Method.GET, "http://localhost/"));
        ChallengeRequest challenge = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");
        challenge.getParameters().add(new Parameter("charset", "UTF-8"));
        response.getChallengeRequests().add(challenge);

        ChallengeResponse cr =
                new ChallengeResponse(challenge, response, UTF8_LOGIN, UTF8_PASSWORD.toCharArray());

        assertEquals(
                "Basic " + base64Of(UTF8_LOGIN, UTF8_PASSWORD, StandardCharsets.UTF_8),
                AuthenticatorUtils.formatResponse(cr, response.getRequest(), null));
    }

    // ---- Case insensitivity of the parameter names (RFC 9110 section 11.2)
    // -----------------------

    @Test
    void parseRequest_parameterNamesInAnyCase_realmAndCharsetRecognized() {
        Response response = new Response(new Request(Method.GET, "http://localhost/"));
        ChallengeRequest challenge =
                AuthenticatorUtils.parseRequest(
                                response, "Basic REALM=\"r\", Charset=\"utf-8\"", null)
                        .get(0);

        assertEquals("r", challenge.getRealm());
        assertEquals("utf-8", challenge.getParameters().getFirstValue("charset", true));

        response.getChallengeRequests().add(challenge);
        ChallengeResponse cr =
                new ChallengeResponse(challenge, response, UTF8_LOGIN, UTF8_PASSWORD.toCharArray());
        assertEquals(
                "Basic " + base64Of(UTF8_LOGIN, UTF8_PASSWORD, StandardCharsets.UTF_8),
                AuthenticatorUtils.formatResponse(cr, response.getRequest(), null));
    }
}
