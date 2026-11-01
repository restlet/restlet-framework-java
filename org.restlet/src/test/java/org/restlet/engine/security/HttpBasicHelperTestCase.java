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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;

/**
 * Unit tests for the credentials charset handling of {@link HttpBasicHelper} (RFC 7617 section
 * 2.1), without any network.
 */
class HttpBasicHelperTestCase {

    @BeforeEach
    void registerEngine() {
        Engine.register(); // other tests may leave an engine without the Basic helper
    }

    private static final String LATIN1_LOGIN = "josé";
    private static final String LATIN1_PASSWORD = "pässwörd";
    private static final String UTF8_LOGIN = "zoë";

    /** The euro sign does not exist in ISO-8859-1. */
    private static final String UTF8_PASSWORD = "p€ss";

    private static String basicHeader(String login, String password, Charset cs) {
        byte[] bytes = (login + ":" + password).getBytes(cs);
        return "Basic " + Base64.getEncoder().encodeToString(bytes);
    }

    private static String base64Of(String login, String password, Charset cs) {
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

    // ---- parseCredentials --------------------------------------------------------------------

    private static ChallengeResponse rawResponse(String login, String password, Charset cs) {
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        cr.setRawValue(base64Of(login, password, cs));
        return cr;
    }

    @Test
    void parseCredentials_invalidInPrimaryWithoutFallback_throws() {
        ChallengeResponse cr =
                rawResponse(LATIN1_LOGIN, LATIN1_PASSWORD, StandardCharsets.ISO_8859_1);
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpBasicHelper.parseCredentials(cr, StandardCharsets.UTF_8, null));
    }

    @Test
    void parseCredentials_iso88591Primary_readsUtf8BytesAsLatin1() {
        ChallengeResponse cr = rawResponse("zoë", "pw", StandardCharsets.UTF_8);
        assertTrue(HttpBasicHelper.parseCredentials(cr, StandardCharsets.ISO_8859_1, null));
        assertEquals("zoÃ«", cr.getIdentifier());
    }

    @Test
    void parseCredentials_noSeparator_returnsFalse() {
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        cr.setRawValue(Base64.getEncoder().encodeToString("nocolon".getBytes()));
        assertFalse(
                HttpBasicHelper.parseCredentials(
                        cr, StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1));
    }
}
