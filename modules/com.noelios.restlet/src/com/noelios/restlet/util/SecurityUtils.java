/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
     * Formats a challenge request as a HTTP header value.
     * 
     * @param request
     *                The challenge request to format.
     * @return The authenticate header value.
     */
    public static String format(ChallengeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme().getTechnicalName());
        
        if(request.getRealm() != null) {
        	sb.append(" realm=\"").append(request.getRealm()).append('"');
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
    public static String format(ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        StringBuilder sb = new StringBuilder();
        sb.append(challenge.getScheme().getTechnicalName()).append(' ');

        String secret = (challenge.getSecret() == null) ? null : new String(
                challenge.getSecret());

        if (challenge.getCredentials() != null) {
            sb.append(challenge.getCredentials());
        } else if (challenge.getScheme().equals(ChallengeScheme.HTTP_AWS)) {
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
