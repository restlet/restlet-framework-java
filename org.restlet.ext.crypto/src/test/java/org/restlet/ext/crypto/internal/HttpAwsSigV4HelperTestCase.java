/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

/**
 * Tests {@link HttpAwsSigV4Helper}. The expected signatures come from the AWS documentation: the
 * Amazon S3 examples of "Signature Calculations for the Authorization Header: Transferring Payload
 * in a Single Chunk" and the generic AWS Signature Version 4 test suite.
 */
class HttpAwsSigV4HelperTestCase {

    private static final String S3_KEY_ID = "AKIAIOSFODNN7EXAMPLE";

    private static final String S3_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private static final String S3_HOST = "examplebucket.s3.amazonaws.com";

    private static final Clock S3_CLOCK =
            Clock.fixed(Instant.parse("2013-05-24T00:00:00Z"), ZoneOffset.UTC);

    private static final String SUITE_KEY_ID = "AKIDEXAMPLE";

    private static final String SUITE_SECRET = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";

    private static final String SUITE_HOST = "example.amazonaws.com";

    private static final Clock SUITE_CLOCK =
            Clock.fixed(Instant.parse("2015-08-30T12:36:00Z"), ZoneOffset.UTC);

    private static ChallengeResponse challenge(
            String keyId, String secret, String region, String service) {
        ChallengeResponse challenge =
                new ChallengeResponse(ChallengeScheme.HTTP_AWS_SIGV4, keyId, secret);
        if (region != null) {
            challenge.getParameters().add(HttpAwsSigV4Helper.PARAM_REGION, region);
        }
        if (service != null) {
            challenge.getParameters().add(HttpAwsSigV4Helper.PARAM_SERVICE, service);
        }
        return challenge;
    }

    private static Series<Header> headers(String host) {
        Series<Header> headers = new Series<>(Header.class);
        headers.add(HeaderConstants.HEADER_HOST, host);
        return headers;
    }

    private static String sign(
            Clock clock, ChallengeResponse challenge, Request request, Series<Header> headers) {
        ChallengeWriter cw = new ChallengeWriter();
        new HttpAwsSigV4Helper(clock).formatResponse(cw, challenge, request, headers);
        return cw.toString();
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test
    void s3GetObject() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/test.txt");
        Series<Header> headers = headers(S3_HOST);
        headers.add(HeaderConstants.HEADER_RANGE, "bytes=0-9");

        String result =
                sign(
                        S3_CLOCK,
                        challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null),
                        request,
                        headers);

        assertEquals(
                "Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request,"
                        + " SignedHeaders=host;range;x-amz-content-sha256;x-amz-date,"
                        + " Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41",
                result);
        assertEquals("20130524T000000Z", headers.getFirstValue("x-amz-date", true));
        assertEquals(
                HttpAwsSigV4Helper.EMPTY_PAYLOAD_HASH,
                headers.getFirstValue("x-amz-content-sha256", true));
    }

    @Test
    void s3GetObjectCanonicalRequestAndStringToSign() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/test.txt");
        Series<Header> headers = headers(S3_HOST);
        headers.add(HeaderConstants.HEADER_RANGE, "bytes=0-9");
        headers.add("x-amz-content-sha256", HttpAwsSigV4Helper.EMPTY_PAYLOAD_HASH);
        headers.add("x-amz-date", "20130524T000000Z");
        Map<String, String> canonicalHeaders = HttpAwsSigV4Helper.getCanonicalHeaders(headers);

        String canonicalRequest =
                HttpAwsSigV4Helper.getCanonicalRequest(
                        "GET",
                        request.getResourceRef(),
                        false,
                        canonicalHeaders,
                        String.join(";", canonicalHeaders.keySet()),
                        HttpAwsSigV4Helper.EMPTY_PAYLOAD_HASH);

        assertEquals(
                """
                GET
                /test.txt

                host:examplebucket.s3.amazonaws.com
                range:bytes=0-9
                x-amz-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
                x-amz-date:20130524T000000Z

                host;range;x-amz-content-sha256;x-amz-date
                e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855""",
                canonicalRequest);
        assertEquals(
                """
                AWS4-HMAC-SHA256
                20130524T000000Z
                20130524/us-east-1/s3/aws4_request
                7344ae5b7ee6c3e7e6b0fe0640412a37625d1fbfff95c48bbb2dc43964946972""",
                HttpAwsSigV4Helper.getStringToSign(
                        "20130524T000000Z",
                        "20130524/us-east-1/s3/aws4_request",
                        canonicalRequest));
    }

    @Test
    void s3PutObject() {
        Request request =
                new Request(
                        Method.PUT,
                        "http://" + S3_HOST + "/test$file.text",
                        new StringRepresentation("Welcome to Amazon S3.", MediaType.TEXT_PLAIN));
        Series<Header> headers = headers(S3_HOST);
        headers.add(HeaderConstants.HEADER_DATE, "Fri, 24 May 2013 00:00:00 GMT");
        headers.add("x-amz-storage-class", "REDUCED_REDUNDANCY");

        String result =
                sign(
                        S3_CLOCK,
                        challenge(S3_KEY_ID, S3_SECRET, "us-east-1", "s3"),
                        request,
                        headers);

        assertEquals(
                "Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request,"
                        + " SignedHeaders=date;host;x-amz-content-sha256;x-amz-date;x-amz-storage-class,"
                        + " Signature=98ad721746da40c64f1a55b78f14c238d841ea1380cd77a1b5971af0ece108bd",
                result);
        assertEquals(
                "44ce7dd67c959e0d3524ffac1771dfbba87d2b6b4b4e99e42034a8b803f8b072",
                headers.getFirstValue("x-amz-content-sha256", true));
    }

    @Test
    void s3GetBucketLifecycle() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/?lifecycle");

        String result =
                sign(
                        S3_CLOCK,
                        challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null),
                        request,
                        headers(S3_HOST));

        assertEquals(
                "Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request,"
                        + " SignedHeaders=host;x-amz-content-sha256;x-amz-date,"
                        + " Signature=fea454ca298b7da1c68078a5d1bdbfbbe0d65c699e0f91ac7a200a0136783543",
                result);
    }

    @Test
    void s3ListObjects() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/?max-keys=2&prefix=J");

        String result =
                sign(
                        S3_CLOCK,
                        challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null),
                        request,
                        headers(S3_HOST));

        assertEquals(
                "Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request,"
                        + " SignedHeaders=host;x-amz-content-sha256;x-amz-date,"
                        + " Signature=34b48302e7b5fa45bde8084f4b7868a86f0a534bc59db6670ed5711ef69dc6f7",
                result);
    }

    @Test
    void suiteGetVanilla() {
        Request request = new Request(Method.GET, "http://" + SUITE_HOST + "/");
        Series<Header> headers = headers(SUITE_HOST);

        String result =
                sign(
                        SUITE_CLOCK,
                        challenge(SUITE_KEY_ID, SUITE_SECRET, "us-east-1", "service"),
                        request,
                        headers);

        assertEquals(
                "Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request,"
                        + " SignedHeaders=host;x-amz-date,"
                        + " Signature=5fa00fa31553b73ebf1942676e86291e8372ff2a2260956d9b8aae1d763fbf31",
                result);
        // Only S3 needs the payload hash header
        assertEquals(null, headers.getFirstValue("x-amz-content-sha256", true));
    }

    @Test
    void suiteGetVanillaQueryOrderKeyCase() {
        Request request =
                new Request(Method.GET, "http://" + SUITE_HOST + "/?Param2=value2&Param1=value1");

        String result =
                sign(
                        SUITE_CLOCK,
                        challenge(SUITE_KEY_ID, SUITE_SECRET, "us-east-1", "service"),
                        request,
                        headers(SUITE_HOST));

        assertEquals(
                "Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request,"
                        + " SignedHeaders=host;x-amz-date,"
                        + " Signature=b97d918cfa904a5beff61c982a1b6f458b799221646efd99d3219ec94cdf2500",
                result);
    }

    @Test
    void keepsAnExistingDateAndAddsTheSessionToken() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/test.txt");
        Series<Header> headers = headers(S3_HOST);
        headers.add("X-Amz-Date", "20130524T000000Z");
        ChallengeResponse challenge = challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null);
        challenge.getParameters().add(HttpAwsSigV4Helper.PARAM_SESSION_TOKEN, "token+/=");

        String result = sign(Clock.systemUTC(), challenge, request, headers);

        assertEquals(1, headers.subList("x-amz-date", true).size());
        assertEquals("token+/=", headers.getFirstValue("x-amz-security-token", true));
        assertTrue(
                result.contains(
                        "SignedHeaders=host;x-amz-content-sha256;x-amz-date;x-amz-security-token,"),
                result);
        assertTrue(result.startsWith("Credential=" + S3_KEY_ID + "/20130524/"), result);
    }

    @Test
    void sendsAnUnsignedPayloadToS3WhenAskedOrForATransientEntity() {
        ChallengeResponse challenge = challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null);
        challenge.getParameters().add(HttpAwsSigV4Helper.PARAM_UNSIGNED_PAYLOAD, "true");
        Request request =
                new Request(
                        Method.PUT,
                        "http://" + S3_HOST + "/a",
                        new StringRepresentation("data", MediaType.TEXT_PLAIN));
        Series<Header> headers = headers(S3_HOST);
        sign(S3_CLOCK, challenge, request, headers);
        assertEquals(
                HttpAwsSigV4Helper.UNSIGNED_PAYLOAD,
                headers.getFirstValue("x-amz-content-sha256", true));

        request.setEntity(
                new InputRepresentation(
                        new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8)),
                        MediaType.TEXT_PLAIN));
        headers = headers(S3_HOST);
        sign(S3_CLOCK, challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null), request, headers);
        assertEquals(
                HttpAwsSigV4Helper.UNSIGNED_PAYLOAD,
                headers.getFirstValue("x-amz-content-sha256", true));
    }

    @Test
    void refusesATransientEntityForOtherServices() {
        Request request =
                new Request(
                        Method.POST,
                        "http://" + SUITE_HOST + "/",
                        new InputRepresentation(
                                new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8)),
                                MediaType.TEXT_PLAIN));
        ChallengeResponse challenge = challenge(SUITE_KEY_ID, SUITE_SECRET, "us-east-1", "service");

        assertThrows(
                IllegalArgumentException.class,
                () -> sign(SUITE_CLOCK, challenge, request, headers(SUITE_HOST)));
    }

    @Test
    void refusesMissingCredentialsOrRegion() {
        Request request = new Request(Method.GET, "http://" + S3_HOST + "/");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        sign(
                                S3_CLOCK,
                                challenge(S3_KEY_ID, S3_SECRET, null, null),
                                request,
                                headers(S3_HOST)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        sign(
                                S3_CLOCK,
                                challenge(S3_KEY_ID, "", "us-east-1", null),
                                request,
                                headers(S3_HOST)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        sign(
                                S3_CLOCK,
                                challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null),
                                request,
                                null));
    }

    @Test
    void addsTheHostHeaderWhenMissing() {
        Request request = new Request(Method.GET, "http://localhost:9000/bucket/key");
        Series<Header> headers = new Series<>(Header.class);

        sign(S3_CLOCK, challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null), request, headers);

        assertEquals("localhost:9000", headers.getFirstValue("host", true));
    }

    @Test
    void canonicalUri() {
        assertEquals("/", HttpAwsSigV4Helper.getCanonicalUri(null, false));
        assertEquals("/", HttpAwsSigV4Helper.getCanonicalUri("", true));
        assertEquals(
                "/documents%20and%20settings/",
                HttpAwsSigV4Helper.getCanonicalUri("/documents and settings/", false));
        assertEquals(
                "/documents%20and%20settings/",
                HttpAwsSigV4Helper.getCanonicalUri("/documents%20and%20settings/", false));
        assertEquals(
                "/documents%2520and%2520settings/",
                HttpAwsSigV4Helper.getCanonicalUri("/documents%20and%20settings/", true));
        assertEquals("/caf%C3%A9", HttpAwsSigV4Helper.getCanonicalUri("/café", false));
        assertEquals("/a%2Bb%25zz", HttpAwsSigV4Helper.getCanonicalUri("/a+b%zz", false));
    }

    @Test
    void canonicalQuery() {
        assertEquals("", HttpAwsSigV4Helper.getCanonicalQuery(null));
        assertEquals("a=0&a=1&b=2&c=", HttpAwsSigV4Helper.getCanonicalQuery("b=2&a=1&&a=0&c"));
        assertEquals(
                "prefix=a%20b%2Bc&x=%2F",
                HttpAwsSigV4Helper.getCanonicalQuery("x=/&prefix=a%20b+c"));
    }

    @Test
    void canonicalHeaders() {
        Series<Header> headers = new Series<>(Header.class);
        headers.add("Host", "h");
        headers.add("User-Agent", "Restlet");
        headers.add("Content-Length", "4");
        headers.add("Connection", "keep-alive");
        headers.add("X-Amz-Meta-A", "  one   two ");
        headers.add("x-amz-meta-a", "three");

        Map<String, String> result = HttpAwsSigV4Helper.getCanonicalHeaders(headers);

        assertEquals(Set.of("host", "x-amz-meta-a"), result.keySet());
        assertEquals("one two,three", result.get("x-amz-meta-a"));
    }

    @Test
    void schemeIsRegistered() {
        assertSame(ChallengeScheme.HTTP_AWS_SIGV4, ChallengeScheme.valueOf("http_aws_sigv4"));
        assertEquals("AWS4-HMAC-SHA256", ChallengeScheme.HTTP_AWS_SIGV4.getTechnicalName());
        HttpAwsSigV4Helper helper = new HttpAwsSigV4Helper();
        assertTrue(helper.isClientSide());
        assertFalse(helper.isServerSide());
    }

    /**
     * Sends a signed request through the client connector and checks, on the server, that the
     * signature matches the headers, path, query and body actually received.
     */
    @Test
    void signsWhatTheClientConnectorSends() throws Exception {
        AtomicReference<Request> received = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();
        Server server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                try {
                                    receivedBody.set(request.getEntity().getText());
                                } catch (Exception e) {
                                    response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                                    return;
                                }
                                received.set(request);
                                response.setStatus(Status.SUCCESS_NO_CONTENT);
                            }
                        });
        server.start();
        Client client = new Client(new Context(), Protocol.HTTP);
        client.start();
        try {
            String body = "{\"note\":\"Welcome to \\\"S3\\\"\"}";
            Request request =
                    new Request(
                            Method.PUT,
                            "http://localhost:"
                                    + server.getActualPort()
                                    + "/bucket/my%20file$1.json?x-id=PutObject&a=b%2Fc",
                            new StringRepresentation(body, MediaType.APPLICATION_JSON));
            request.getHeaders().add("x-amz-meta-owner", "physics");
            request.setChallengeResponse(challenge(S3_KEY_ID, S3_SECRET, "us-east-1", null));

            Response response = client.handle(request);
            assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

            Request seen = received.get();
            assertNotNull(seen);
            Series<Header> seenHeaders = seen.getHeaders();
            String authorization = seenHeaders.getFirstValue("Authorization", true);
            assertTrue(authorization.startsWith("AWS4-HMAC-SHA256 Credential="), authorization);

            String signedHeaders = authorization.replaceAll(".*SignedHeaders=([^,]+),.*", "$1");
            String signature = authorization.replaceAll(".*Signature=([0-9a-f]+)$", "$1");
            Set<String> signed = Set.of(signedHeaders.split(";"));
            assertTrue(
                    signed.containsAll(
                            Set.of(
                                    "host",
                                    "content-type",
                                    "x-amz-content-sha256",
                                    "x-amz-date",
                                    "x-amz-meta-owner")),
                    signedHeaders);

            String payloadHash = seenHeaders.getFirstValue("x-amz-content-sha256", true);
            assertEquals(
                    sha256Hex(receivedBody.get().getBytes(StandardCharsets.UTF_8)), payloadHash);

            Series<Header> signedSeen = new Series<>(Header.class);
            for (Header header : seenHeaders) {
                if (signed.contains(header.getName().toLowerCase())) {
                    signedSeen.add(header);
                }
            }
            Map<String, String> canonicalHeaders =
                    HttpAwsSigV4Helper.getCanonicalHeaders(signedSeen);
            assertEquals(signedHeaders, String.join(";", canonicalHeaders.keySet()));

            String amzDate = seenHeaders.getFirstValue("x-amz-date", true);
            String scope = amzDate.substring(0, 8) + "/us-east-1/s3/aws4_request";
            String canonicalRequest =
                    HttpAwsSigV4Helper.getCanonicalRequest(
                            seen.getMethod().getName(),
                            seen.getResourceRef(),
                            false,
                            canonicalHeaders,
                            signedHeaders,
                            payloadHash);
            assertEquals(
                    signature,
                    HttpAwsSigV4Helper.getSignature(
                            S3_SECRET.toCharArray(),
                            amzDate.substring(0, 8),
                            "us-east-1",
                            "s3",
                            HttpAwsSigV4Helper.getStringToSign(amzDate, scope, canonicalRequest)));
        } finally {
            client.stop();
            server.stop();
        }
    }
}
