/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.CredentialException;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.util.DateUtils;
import org.restlet.util.Series;

import com.noelios.restlet.Engine;
import com.noelios.restlet.http.HttpConstants;

/**
 * Security data manipulation utilities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SecurityUtils {
    /**
     * General regex pattern to extract comma separated name-value directives.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all directives. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this.
     */
    private static final Pattern directivesPattern = Pattern
            .compile("([^=]+)=\"?([^\",]+)(?:\"\\s*)?,?\\s*");

    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this.
     */
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    /**
     * Returns true if one of the provided object is null.
     * 
     * @param objects
     *                a sequence of objects
     * @return true if one of the provided object is null.
     */
    public static boolean anyNull(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Formats a challenge request as a HTTP header value.
     * 
     * @param request
     *                The challenge request to format.
     * @return The authenticate header value.
     */
    public static String format(ChallengeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme().getTechnicalName());

        if (request.getRealm() != null) {
            sb.append(" realm=\"").append(request.getRealm()).append('"');

            // Manage the DIGEST authentication particularities
            if (request.getScheme().equals(ChallengeScheme.HTTP_DIGEST)) {
                Series<Parameter> parameters = request.getParameters();
                sb.append(", domain=\"").append(
                        parameters.getFirstValue("domain")).append('"');
                sb.append(", qop=\"auth\"");
                // leave this value unquoted as per RFC-2617
                sb.append(", algorithm=MD5");
                sb.append(", nonce=\"").append(
                        parameters.getFirstValue("nonce")).append('"');

                if (parameters.getFirst("stale") != null) {
                    sb.append(", stale=\"TRUE\"");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Formats a challenge response as raw credentials.
     * 
     * @param challenge
     *                The challenge response to format.
     * @param request
     *                The parent request.
     * @param httpHeaders
     *                The current request HTTP headers.
     * @return The authorization header value.
     */
    @SuppressWarnings("deprecation")
    public static String format(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        StringBuilder sb = new StringBuilder();
        sb.append(challenge.getScheme().getTechnicalName()).append(' ');

        String secret = (challenge.getSecret() == null) ? null : new String(
                challenge.getSecret());

        if (challenge.getCredentials() != null) {
            sb.append(challenge.getCredentials());
        } else if (challenge.getScheme().equals(ChallengeScheme.HTTP_AWS)
                || challenge.getScheme().equals(ChallengeScheme.HTTP_AWS_S3)) {
            // Setup the method name
            String methodName = request.getMethod().getName();

            // Setup the Date header
            String date = "";

            if (httpHeaders.getFirstValue("X-Amz-Date", true) == null) {
                // X-Amz-Date header didn't override the standard Date header
                date = httpHeaders.getFirstValue(HttpConstants.HEADER_DATE,
                        true);
                if (date == null) {
                    // Add a fresh Date header
                    date = DateUtils.format(new Date(),
                            DateUtils.FORMAT_RFC_1123.get(0));
                    httpHeaders.add(HttpConstants.HEADER_DATE, date);
                }
            }

            // Setup the ContentType header
            String contentMd5 = httpHeaders.getFirstValue(
                    HttpConstants.HEADER_CONTENT_MD5, true);
            if (contentMd5 == null)
                contentMd5 = "";

            // Setup the ContentType header
            String contentType = httpHeaders.getFirstValue(
                    HttpConstants.HEADER_CONTENT_TYPE, true);
            if (contentType == null) {
                boolean applyPatch = false;

                // This patch seems to apply to Sun JVM only.
                String jvmVendor = System.getProperty("java.vm.vendor");
                if (jvmVendor != null
                        && (jvmVendor.toLowerCase()).startsWith("sun")) {
                    int majorVersionNumber = Engine.getJavaMajorVersion();
                    int minorVersionNumber = Engine.getJavaMinorVersion();

                    if (majorVersionNumber == 1) {
                        if (minorVersionNumber < 5) {
                            applyPatch = true;
                        } else if (minorVersionNumber == 5) {
                            // Sun fixed the bug in update 10
                            applyPatch = (Engine.getJavaUpdateVersion() < 10);
                        }
                    }
                }

                if (applyPatch && !request.getMethod().equals(Method.PUT)) {
                    contentType = "application/x-www-form-urlencoded";
                } else {
                    contentType = "";
                }
            }

            // Setup the canonicalized AmzHeaders
            String canonicalizedAmzHeaders = getCanonicalizedAmzHeaders(httpHeaders);

            // Setup the canonicalized resource name
            String canonicalizedResource = getCanonicalizedResourceName(request
                    .getResourceRef());

            // Setup the message part
            StringBuilder rest = new StringBuilder();
            rest.append(methodName).append('\n').append(contentMd5)
                    .append('\n').append(contentType).append('\n').append(date)
                    .append('\n').append(canonicalizedAmzHeaders).append(
                            canonicalizedResource);

            // Append the AWS credentials
            sb.append(challenge.getIdentifier()).append(':').append(
                    Base64.encodeBytes(toHMac(rest.toString(), secret),
                            Base64.DONT_BREAK_LINES));
        } else if (challenge.getScheme().equals(ChallengeScheme.HTTP_BASIC)) {
            try {
                String credentials = challenge.getIdentifier() + ':' + secret;
                sb.append(Base64.encodeBytes(credentials.getBytes("US-ASCII"),
                        Base64.DONT_BREAK_LINES));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(
                        "Unsupported encoding, unable to encode credentials");
            }
        } else if (challenge.getScheme().equals(ChallengeScheme.HTTP_DIGEST)) {
            Series<Parameter> credentials = challenge.getParameters();

            for (String name : credentials.getNames()) {
                sb.append(name).append('=');
                if (name.equals("qop") || name.equals("algorithm")
                        || name.equals("nc")) {
                    // these values are left unquoted as per RC2617
                    sb.append(credentials.getFirstValue(name)).append(",");
                } else {
                    sb.append('"').append(credentials.getFirstValue(name))
                            .append('"').append(",");
                }
            }

            if (!credentials.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }
        } else if (challenge.getScheme().equals(ChallengeScheme.SMTP_PLAIN)) {
            try {
                String credentials = "^@" + challenge.getIdentifier() + "^@"
                        + secret;
                sb.append(Base64.encodeBytes(credentials.getBytes("US-ASCII"),
                        Base64.DONT_BREAK_LINES));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(
                        "Unsupported encoding, unable to encode credentials");
            }
        } else {
            throw new IllegalArgumentException(
                    "Challenge scheme not supported by this implementation, or credentials not set for custom schemes.");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized AMZ headers.
     * 
     * @param requestHeaders
     *                The list of request headers.
     * @return The canonicalized AMZ headers.
     */
    private static String getCanonicalizedAmzHeaders(
            Series<Parameter> requestHeaders) {
        // Filter out all the AMZ headers required for AWS authentication
        SortedMap<String, String> amzHeaders = new TreeMap<String, String>();
        String headerName;
        for (Parameter param : requestHeaders) {
            headerName = param.getName().toLowerCase();
            if (headerName.startsWith("x-amz-")) {
                if (!amzHeaders.containsKey(headerName)) {
                    amzHeaders.put(headerName, requestHeaders
                            .getValues(headerName));
                }
            }
        }

        // Concatenate all AMZ headers
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : amzHeaders.entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue())
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     * 
     * @param resourceRef
     *                The resource reference.
     * @return The canonicalized resource name.
     */
    private static String getCanonicalizedResourceName(Reference resourceRef) {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceRef.getPath());

        Form query = resourceRef.getQueryAsForm();
        if (query.getFirst("acl", true) != null) {
            sb.append("?acl");
        } else if (query.getFirst("torrent", true) != null) {
            sb.append("?torrent");
        }

        return sb.toString();
    }

    /**
     * Checks whether the specified nonce is valid with respect to the specified
     * secretKey, and further confirms that the nonce was generated less than
     * lifespanMillis milliseconds ago
     * 
     * @param nonce
     *                The nonce value to check.
     * @param secretKey
     *                The same secret value that was inserted into the nonce
     *                when it was generated.
     * @param lifespanMS
     *                Nonce lifespace in milliseconds.
     * @return True if the nonce was generated less than lifespanMS milliseconds
     *         ago, false otherwise.
     * @throws CredentialException
     *                 if the nonce does not match the specified secretKey, or
     *                 if it can't be parsed.
     */
    public static boolean isNonceValid(String nonce, String secretKey,
            long lifespanMS) throws CredentialException {
        try {
            String decodedNonce = new String(Base64.decode(nonce));
            long nonceTimeMS = Long.parseLong(decodedNonce.substring(0,
                    decodedNonce.indexOf(':')));
            if (decodedNonce.equals(nonceTimeMS + ":"
                    + md5String(nonceTimeMS + ":" + secretKey))) {
                // valid wrt secretKey, now check lifespan
                return lifespanMS > (System.currentTimeMillis() - nonceTimeMS);
            }
        } catch (Exception e) {
            throw new CredentialException("Error parsing nonce: " + e);
        }

        throw new CredentialException("Nonce does not match secret key!");
    }

    /**
     * Generates a nonce as recommended in section 3.2.1 of RFC-2617, but
     * without the ETag field. The format is: <code><pre>
     * Base64.encodeBytes(currentTimeMS + &quot;:&quot;
     *         + md5String(currentTimeMS + &quot;:&quot; + secretKey))
     * </pre></code>
     * 
     * @param secretKey
     *                A secret value known only by the creator of the nonce.
     *                It's inserted into the nonce, and can be used later to
     *                validate the nonce.
     * @return The nonce value.
     */
    public static String makeNonce(String secretKey) {
        long currentTimeMS = System.currentTimeMillis();
        return Base64.encodeBytes(
                (currentTimeMS + ":" + md5String(currentTimeMS + ":"
                        + secretKey)).getBytes(), Base64.DONT_BREAK_LINES);
    }

    /**
     * Returns the MD5 digest of target string. Target is decoded to bytes using
     * the US-ASCII charset. The returned hexidecimal String always contains 32
     * lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *                The target string
     * @return The target string digested.
     */
    public static String md5String(String target) {
        try {
            return md5String(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // unlikely, US-ASCII comes with every JVM
            throw new RuntimeException(
                    "US-ASCII is an unsupported encoding, unable to compute MD5");
        }
    }

    /**
     * Returns the MD5 digest of target string. Target is decoded to bytes using
     * the named charset. The returned hexidecimal String always contains 32
     * lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *                The target string.
     * @param charsetName
     *                The charset used when applying the MD5 digest.
     * @return The target string digested.
     * @throws UnsupportedEncodingException
     */
    public static String md5String(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(
                    target.getBytes(charsetName));
            char[] md5Chars = new char[32];
            int i = 0;
            for (byte b : md5) {
                md5Chars[i++] = HEXDIGITS[(b >> 4) & 0xF];
                md5Chars[i++] = HEXDIGITS[b & 0xF];
            }
            return new String(md5Chars);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "No MD5 algorithm, unable to compute MD5");
        }
    }

    /**
     * Parse a credentials string, capture all name-value components and add
     * them to the provided directives collection.
     * 
     * @param credentials
     *                The credentials string to parse.
     * @param directives
     *                The collection of directives to update.
     */
    public static void parseDirectives(String credentials,
            Series<Parameter> directives) {
        Matcher matcher = directivesPattern.matcher(credentials);

        while (matcher.find() && matcher.groupCount() == 2) {
            directives.add(matcher.group(1), matcher.group(2));
        }
    }

    /**
     * Parses an authenticate header into a challenge request.
     * 
     * @param header
     *                The HTTP header value to parse.
     * @return The parsed challenge request.
     */
    public static ChallengeRequest parseRequest(String header) {
        ChallengeRequest result = null;

        if (header != null) {
            int space = header.indexOf(' ');

            if (space != -1) {
                String scheme = header.substring(0, space);
                String realm = header.substring(space + 1);
                int equals = realm.indexOf('=');
                String realmValue = realm.substring(equals + 2,
                        realm.length() - 1);
                result = new ChallengeRequest(new ChallengeScheme("HTTP_"
                        + scheme, scheme), realmValue);

                // Parse DIGEST authentication parameters.
                if (result.getScheme().equals(ChallengeScheme.HTTP_DIGEST)) {
                    Series<Parameter> parameters = new Form();
                    SecurityUtils.parseDirectives(realm, parameters);
                    result.setRealm(parameters.getFirstValue("realm"));
                    parameters.removeAll("realm");
                    result.setParameters(parameters);
                }
            }
        }

        return result;
    }

    /**
     * Parses an authorization header into a challenge response.
     * 
     * @param request
     *                The request.
     * @param logger
     *                The logger to use.
     * @param header
     *                The header value to parse.
     * @return The parsed challenge response.
     */
    public static ChallengeResponse parseResponse(Request request,
            Logger logger, String header) {
        ChallengeResponse result = null;

        if (header != null) {
            int space = header.indexOf(' ');

            if (space != -1) {
                String scheme = header.substring(0, space);
                String credentials = header.substring(space + 1);
                result = new ChallengeResponse(new ChallengeScheme("HTTP_"
                        + scheme, scheme), credentials);

                if (result.getScheme().equals(ChallengeScheme.HTTP_BASIC)) {
                    try {
                        byte[] credentialsEncoded = Base64.decode(result
                                .getCredentials());
                        if (credentialsEncoded == null) {
                            logger.warning("Cannot decode credentials: "
                                    + result.getCredentials());
                            return null;
                        }

                        credentials = new String(credentialsEncoded, "US-ASCII");
                        int separator = credentials.indexOf(':');

                        if (separator == -1) {
                            // Log the blocking
                            logger
                                    .warning("Invalid credentials given by client with IP: "
                                            + ((request != null) ? request
                                                    .getClientInfo()
                                                    .getAddress() : "?"));
                        } else {
                            result.setIdentifier(credentials.substring(0,
                                    separator));
                            result.setSecret(credentials
                                    .substring(separator + 1));

                            // Log the authentication result
                            if (logger != null) {
                                logger
                                        .info("Basic HTTP authentication succeeded: identifier="
                                                + result.getIdentifier() + ".");
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        logger.log(Level.WARNING, "Unsupported encoding error",
                                e);
                    }
                } else if (result.getScheme().equals(
                        ChallengeScheme.HTTP_DIGEST)) {
                    SecurityUtils.parseDirectives(credentials, result
                            .getParameters());
                } else {
                    // Authentication impossible, scheme not supported
                    logger
                            .log(
                                    Level.FINE,
                                    "Authentication impossible: scheme not supported: "
                                            + result.getScheme().getName()
                                            + ". Please override the Guard.authenticate method.");
                }
            }
        }

        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     * 
     * @param source
     *                The source string to convert.
     * @param secretKey
     *                The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac(String source, String secretKey) {
        byte[] result = null;

        try {
            // Create the HMAC/SHA1 key
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(),
                    "HmacSHA1");

            // Create the message authentication code (MAC)
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the HMAC value
            result = mac.doFinal(source.getBytes());
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "Could not find the SHA-1 algorithm. HMac conversion failed.",
                    nsae);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(
                    "Invalid key exception detected. HMac conversion failed.",
                    ike);
        }

        return result;
    }

}
