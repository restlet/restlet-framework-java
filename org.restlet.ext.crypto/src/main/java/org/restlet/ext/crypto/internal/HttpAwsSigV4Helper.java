/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Implements the client side of the AWS Signature Version 4 HTTP authentication ({@link
 * ChallengeScheme#HTTP_AWS_SIGV4}), used by Amazon S3, the other AWS services and the S3-compatible
 * object stores.
 *
 * <p>The identifier of the {@link ChallengeResponse} is the access key ID and its secret is the
 * secret access key. Its parameters give:
 *
 * <ul>
 *   <li>{@value #PARAM_REGION} (required): the region, e.g. {@code us-east-1}.
 *   <li>{@value #PARAM_SERVICE}: the service name, {@value #DEFAULT_SERVICE} by default.
 *   <li>{@value #PARAM_SESSION_TOKEN}: the session token of temporary credentials, sent in the
 *       {@code X-Amz-Security-Token} header.
 *   <li>{@value #PARAM_UNSIGNED_PAYLOAD}: {@code true} to send {@value #UNSIGNED_PAYLOAD} instead
 *       of hashing the entity (S3 only).
 * </ul>
 *
 * <p>The helper adds the {@code X-Amz-Date} header, unless the request already has one, and for S3
 * the {@code X-Amz-Content-Sha256} header. A request entity is hashed when it can be read again
 * ({@link Representation#isTransient()} is {@code false}). A transient entity is sent as {@value
 * #UNSIGNED_PAYLOAD} to S3 and is refused for the other services, which require the hash.
 *
 * <p>Every request header present when the {@code Authorization} header is written is signed,
 * except the hop-by-hop headers, {@code Content-Length} and {@code User-Agent}, which a transport
 * or a proxy may change. A header replaced afterwards, for example through the {@link
 * HeaderConstants#ATTRIBUTE_RAW_REQUEST_HEADERS} attribute, invalidates the signature.
 *
 * <p>The signed path and query are those the client connector sends: a relative reference is
 * resolved against its base. S3 signs the path as is, each segment decoded then URI-encoded once.
 * The other services sign it normalized (empty and dot segments removed) and URI-encoded once more
 * as sent, so an escape in the path is encoded again. Query parameters are signed sorted and
 * URI-encoded: a {@code +} in the query is a literal plus sign, so encode spaces as {@code %20}.
 *
 * @see <a
 *     href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv-create-signed-request.html">Create
 *     a signed AWS API request</a>
 */
public class HttpAwsSigV4Helper extends AuthenticatorHelper {

    /** The name of the parameter giving the region. */
    public static final String PARAM_REGION = "region";

    /** The name of the parameter giving the service. */
    public static final String PARAM_SERVICE = "service";

    /** The name of the parameter giving the session token of temporary credentials. */
    public static final String PARAM_SESSION_TOKEN = "sessionToken";

    /** The name of the parameter asking not to hash the entity (S3 only). */
    public static final String PARAM_UNSIGNED_PAYLOAD = "unsignedPayload";

    /** The default service. */
    public static final String DEFAULT_SERVICE = "s3";

    /** The payload hash sent instead of the entity's hash (S3 only). */
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    /** The signing algorithm. */
    static final String ALGORITHM = "AWS4-HMAC-SHA256";

    /** The hex SHA-256 hash of an empty payload. */
    static final String EMPTY_PAYLOAD_HASH =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    /** The header giving the signing date and time. */
    static final String HEADER_AMZ_DATE = "X-Amz-Date";

    /** The header giving the payload hash, required by S3. */
    static final String HEADER_AMZ_CONTENT_SHA256 = "X-Amz-Content-Sha256";

    /** The header giving the session token of temporary credentials. */
    static final String HEADER_AMZ_SECURITY_TOKEN = "X-Amz-Security-Token";

    /** The headers never signed, in lower case. */
    private static final Set<String> UNSIGNED_HEADERS =
            Set.of(
                    "authorization",
                    "connection",
                    "content-length",
                    "expect",
                    "keep-alive",
                    "proxy-authorization",
                    "proxy-connection",
                    "te",
                    "trailer",
                    "transfer-encoding",
                    "upgrade",
                    "user-agent",
                    "x-amzn-trace-id");

    /** The format of the X-Amz-Date header. */
    private static final DateTimeFormatter AMZ_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT)
                    .withZone(ZoneOffset.UTC);

    private static final HexFormat HEX = HexFormat.of();

    private static final HexFormat HEX_UPPER = HexFormat.of().withUpperCase();

    /** The clock giving the signing time. */
    private final Clock clock;

    /** Constructor. */
    public HttpAwsSigV4Helper() {
        this(Clock.systemUTC());
    }

    /**
     * Constructor.
     *
     * @param clock The clock giving the signing time.
     */
    HttpAwsSigV4Helper(Clock clock) {
        super(ChallengeScheme.HTTP_AWS_SIGV4, true, false);
        this.clock = clock;
    }

    @Override
    public void formatResponse(
            ChallengeWriter cw,
            ChallengeResponse challenge,
            Request request,
            Series<Header> httpHeaders) {
        if (httpHeaders == null) {
            throw new IllegalArgumentException("AWS SigV4 signing needs the request headers");
        }

        String accessKeyId = challenge.getIdentifier();
        char[] secret = challenge.getSecret();
        if (accessKeyId == null || accessKeyId.isEmpty() || secret == null || secret.length == 0) {
            throw new IllegalArgumentException(
                    "AWS SigV4 signing needs an access key ID and a secret access key");
        }

        Series<org.restlet.data.Parameter> params = challenge.getParameters();
        String region = params.getFirstValue(PARAM_REGION);
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException(
                    "AWS SigV4 signing needs the '" + PARAM_REGION + "' parameter");
        }
        String service = params.getFirstValue(PARAM_SERVICE);
        if (service == null || service.isBlank()) {
            service = DEFAULT_SERVICE;
        }
        boolean s3 = DEFAULT_SERVICE.equals(service);

        // Signing date and time
        String amzDate = httpHeaders.getFirstValue(HEADER_AMZ_DATE, true);
        if (amzDate == null || amzDate.isBlank()) {
            amzDate = AMZ_DATE_FORMAT.format(clock.instant());
            httpHeaders.set(HEADER_AMZ_DATE, amzDate, true);
        } else if (amzDate.length() < 8) {
            throw new IllegalArgumentException("Invalid " + HEADER_AMZ_DATE + " header");
        }

        // Temporary credentials
        String sessionToken = params.getFirstValue(PARAM_SESSION_TOKEN);
        String tokenHeader = httpHeaders.getFirstValue(HEADER_AMZ_SECURITY_TOKEN, true);
        if (sessionToken != null
                && !sessionToken.isBlank()
                && (tokenHeader == null || tokenHeader.isBlank())) {
            httpHeaders.set(HEADER_AMZ_SECURITY_TOKEN, sessionToken, true);
        }

        // Payload hash
        String payloadHash = httpHeaders.getFirstValue(HEADER_AMZ_CONTENT_SHA256, true);
        if (payloadHash == null || payloadHash.isBlank()) {
            if (s3 && Boolean.parseBoolean(params.getFirstValue(PARAM_UNSIGNED_PAYLOAD))) {
                payloadHash = UNSIGNED_PAYLOAD;
            } else {
                payloadHash = getPayloadHash(request.getEntity());
                if (payloadHash == null) {
                    if (!s3) {
                        throw new IllegalArgumentException(
                                "AWS SigV4 signing for the '"
                                        + service
                                        + "' service needs an entity that can be read again");
                    }
                    payloadHash = UNSIGNED_PAYLOAD;
                }
            }
            if (s3) {
                httpHeaders.set(HEADER_AMZ_CONTENT_SHA256, payloadHash, true);
            }
        }

        // Host, normally added before the authorization header
        if (httpHeaders.getFirstValue(HeaderConstants.HEADER_HOST, true) == null) {
            String host = getHost(request);
            if (host != null) {
                httpHeaders.add(HeaderConstants.HEADER_HOST, host);
            }
        }

        // Sign the reference the client connector sends: a relative one is resolved against its
        // base, as ReferenceUtils.update does, and an absolute one is kept as is.
        Reference resourceRef = request.getResourceRef();
        if (!resourceRef.isAbsolute()) {
            resourceRef = resourceRef.getTargetRef();
        }

        Map<String, String> canonicalHeaders = getCanonicalHeaders(httpHeaders);
        String signedHeaders = String.join(";", canonicalHeaders.keySet());
        String canonicalRequest =
                getCanonicalRequest(
                        request.getMethod().getName(),
                        resourceRef,
                        !s3,
                        canonicalHeaders,
                        signedHeaders,
                        payloadHash);
        String scope = amzDate.substring(0, 8) + "/" + region + "/" + service + "/aws4_request";
        String stringToSign = getStringToSign(amzDate, scope, canonicalRequest);
        String signature =
                getSignature(secret, amzDate.substring(0, 8), region, service, stringToSign);

        cw.append("Credential=")
                .append(accessKeyId)
                .append('/')
                .append(scope)
                .append(", SignedHeaders=")
                .append(signedHeaders)
                .append(", Signature=")
                .append(signature);
    }

    /**
     * Returns the canonical headers to sign, by lower-case name, sorted. The values of a repeated
     * header are joined with commas, and each value is trimmed with its inner whitespace runs
     * collapsed to one space.
     *
     * @param httpHeaders The request headers.
     * @return The canonical headers to sign.
     */
    static Map<String, String> getCanonicalHeaders(Series<Header> httpHeaders) {
        Map<String, String> result = new TreeMap<>();
        for (Header header : httpHeaders) {
            String name = header.getName().toLowerCase(Locale.ROOT);
            if (!UNSIGNED_HEADERS.contains(name)) {
                String value =
                        header.getValue() == null
                                ? ""
                                : header.getValue().trim().replaceAll("\\s+", " ");
                result.merge(name, value, (a, b) -> a + "," + b);
            }
        }
        return result;
    }

    /**
     * Returns the canonical query string: the parameters URI-encoded, sorted by name then value.
     *
     * @param rawQuery The raw (encoded) query, or null.
     * @return The canonical query string.
     */
    static String getCanonicalQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return "";
        }
        List<String[]> pairs = new ArrayList<>();
        for (String param : rawQuery.split("&")) {
            if (!param.isEmpty()) {
                int eq = param.indexOf('=');
                String name = eq < 0 ? param : param.substring(0, eq);
                String value = eq < 0 ? "" : param.substring(eq + 1);
                pairs.add(
                        new String[] {
                            uriEncode(percentDecode(name)), uriEncode(percentDecode(value))
                        });
            }
        }
        pairs.sort((a, b) -> a[0].equals(b[0]) ? a[1].compareTo(b[1]) : a[0].compareTo(b[0]));
        StringBuilder sb = new StringBuilder();
        for (String[] pair : pairs) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(pair[0]).append('=').append(pair[1]);
        }
        return sb.toString();
    }

    /**
     * Returns the canonical request.
     *
     * @param method The method name.
     * @param resourceRef The target reference.
     * @param doubleEncodePath True to URI-encode the path segments twice (all services but S3).
     * @param canonicalHeaders The canonical headers.
     * @param signedHeaders The signed header names, separated by semicolons.
     * @param payloadHash The payload hash.
     * @return The canonical request.
     */
    static String getCanonicalRequest(
            String method,
            Reference resourceRef,
            boolean doubleEncodePath,
            Map<String, String> canonicalHeaders,
            String signedHeaders,
            String payloadHash) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append('\n');
        sb.append(getCanonicalUri(resourceRef.getPath(), doubleEncodePath)).append('\n');
        sb.append(getCanonicalQuery(resourceRef.getQuery())).append('\n');
        canonicalHeaders.forEach(
                (name, value) -> sb.append(name).append(':').append(value).append('\n'));
        sb.append('\n');
        sb.append(signedHeaders).append('\n');
        sb.append(payloadHash);
        return sb.toString();
    }

    /**
     * Returns the canonical URI.
     *
     * <p>S3 decodes the path it receives and encodes it again: each segment is decoded, then
     * URI-encoded once, and the path is otherwise signed as sent.
     *
     * <p>The other services encode the path they receive once more ("double encoding"): the path is
     * normalized (see {@link #normalizePath(String)}) and each raw segment is URI-encoded as is, so
     * an escape already in the path is encoded again ({@code %7E} is signed as {@code %257E}) and a
     * character sent unescaped is encoded once ({@code ~} stays {@code ~}).
     *
     * @param rawPath The raw (encoded) path, or null.
     * @param doubleEncode True for the services other than S3.
     * @return The canonical URI.
     */
    static String getCanonicalUri(String rawPath, boolean doubleEncode) {
        if (rawPath == null || rawPath.isEmpty()) {
            return "/";
        }
        String path = doubleEncode ? normalizePath(rawPath) : rawPath;
        String[] segments = path.split("/", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(uriEncode(doubleEncode ? segments[i] : percentDecode(segments[i])));
        }
        return sb.charAt(0) == '/' ? sb.toString() : "/" + sb;
    }

    /**
     * Normalizes a path as AWS SigV4 requires for the services other than S3: empty and {@code .}
     * segments are removed and a {@code ..} segment removes the previous one. The path keeps a
     * trailing slash when it ends with a slash or with a {@code .} or {@code ..} segment, as in RFC
     * 3986. For example {@code /a//b/./c/../d} becomes {@code /a/b/d}, {@code //example//} becomes
     * {@code /example/} and {@code /a/b/..} becomes {@code /a/}.
     *
     * @param rawPath The raw path.
     * @return The normalized path, starting with a slash.
     */
    static String normalizePath(String rawPath) {
        Deque<String> segments = new ArrayDeque<>();
        String[] rawSegments = rawPath.split("/", -1);
        for (String segment : rawSegments) {
            if (segment.equals("..")) {
                segments.pollLast();
            } else if (!segment.isEmpty() && !segment.equals(".")) {
                segments.addLast(segment);
            }
        }
        String last = rawSegments[rawSegments.length - 1];
        boolean trailingSlash = last.isEmpty() || last.equals(".") || last.equals("..");
        String result = "/" + String.join("/", segments);
        return trailingSlash && !result.endsWith("/") ? result + "/" : result;
    }

    /**
     * Returns the hex SHA-256 hash of an entity, or null when it can't be read again.
     *
     * @param entity The entity, or null.
     * @return The hex SHA-256 hash, or null.
     */
    static String getPayloadHash(Representation entity) {
        if (entity == null || !entity.isAvailable() || entity.getSize() == 0) {
            return EMPTY_PAYLOAD_HASH;
        }
        if (entity.isTransient()) {
            return null;
        }
        MessageDigest digest = sha256();
        try (InputStream in = entity.getStream()) {
            if (in != null) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to hash the request entity", e);
        }
        return HEX.formatHex(digest.digest());
    }

    /**
     * Returns the hex signature of a string to sign.
     *
     * @param secret The secret access key.
     * @param date The date, as yyyyMMdd.
     * @param region The region.
     * @param service The service.
     * @param stringToSign The string to sign.
     * @return The hex signature.
     */
    static String getSignature(
            char[] secret, String date, String region, String service, String stringToSign) {
        char[] prefixed = new char[secret.length + 4];
        "AWS4".getChars(0, 4, prefixed, 0);
        System.arraycopy(secret, 0, prefixed, 4, secret.length);
        ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(prefixed));
        byte[] key = new byte[encoded.remaining()];
        encoded.get(key);
        Arrays.fill(prefixed, '\0');
        if (encoded.hasArray()) {
            Arrays.fill(encoded.array(), (byte) 0);
        }

        try {
            byte[] dateKey = DigestUtils.toHMacSha256(date, key);
            byte[] regionKey = DigestUtils.toHMacSha256(region, dateKey);
            byte[] serviceKey = DigestUtils.toHMacSha256(service, regionKey);
            byte[] signingKey = DigestUtils.toHMacSha256("aws4_request", serviceKey);
            return HEX.formatHex(DigestUtils.toHMacSha256(stringToSign, signingKey));
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Returns the string to sign.
     *
     * @param amzDate The signing date and time.
     * @param scope The credential scope.
     * @param canonicalRequest The canonical request.
     * @return The string to sign.
     */
    static String getStringToSign(String amzDate, String scope, String canonicalRequest) {
        return ALGORITHM
                + "\n"
                + amzDate
                + "\n"
                + scope
                + "\n"
                + HEX.formatHex(sha256().digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Returns the Host header value of a request: its host, with its port when not the default.
     *
     * @param request The request.
     * @return The Host header value, or null.
     */
    private static String getHost(Request request) {
        Reference ref = request.getResourceRef();
        String host = ref.getHostDomain();
        if (host == null) {
            return null;
        }
        int port = ref.getHostPort();
        Protocol protocol = ref.getSchemeProtocol();
        if (port != -1 && (protocol == null || port != protocol.getDefaultPort())) {
            host = host + ':' + port;
        }
        return host;
    }

    /**
     * Decodes the %XX sequences of a string as UTF-8. Other characters, including {@code +}, are
     * kept.
     *
     * @param value The value to decode.
     * @return The decoded value.
     */
    static String percentDecode(String value) {
        if (value.indexOf('%') < 0) {
            return value;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(value.length());
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '%'
                    && i + 2 < value.length()
                    && HexFormat.isHexDigit(value.charAt(i + 1))
                    && HexFormat.isHexDigit(value.charAt(i + 2))) {
                out.write(HexFormat.fromHexDigits(value, i + 1, i + 3));
                i += 3;
            } else {
                int end = Character.isHighSurrogate(c) && i + 1 < value.length() ? i + 2 : i + 1;
                out.writeBytes(value.substring(i, end).getBytes(StandardCharsets.UTF_8));
                i = end;
            }
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /**
     * URI-encodes a value as AWS SigV4 requires: the unreserved characters (letters, digits, {@code
     * -._~}) are kept, every other UTF-8 byte is written as %XX in upper case.
     *
     * @param value The value to encode.
     * @return The encoded value.
     */
    static String uriEncode(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xff;
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-'
                    || c == '.'
                    || c == '_'
                    || c == '~') {
                sb.append((char) c);
            } else {
                sb.append('%').append(HEX_UPPER.toHexDigits((byte) c));
            }
        }
        return sb.toString();
    }
}
