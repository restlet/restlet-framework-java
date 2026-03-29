/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.restlet.engine.util.StringUtils.isNullOrEmpty;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Provides utility functions for implementing the Amazon S3 Authentication scheme.
 *
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 * @see <a href="http://docs.amazonwebservices.com/AmazonS3/latest/RESTAuthentication.html">
 *     Authenticating REST Requests</a>
 */
public class AwsUtils {

    /**
     * Returns the canonicalized AMZ headers.
     *
     * @param requestHeaders The list of request headers.
     * @return The canonicalized AMZ headers.
     */
    public static String getCanonicalizedAmzHeaders(Series<Header> requestHeaders) {
        StringBuilder sb = new StringBuilder();
        Pattern spacePattern = Pattern.compile("\\s+");

        // Create a lexicographically sorted list of headers that begin with x-amz
        SortedMap<String, String> amzHeaders = new TreeMap<>();

        if (requestHeaders != null) {
            for (Header header : requestHeaders) {
                String name = header.getName().toLowerCase();

                if (name.startsWith("x-amz-")) {
                    String value;

                    if (amzHeaders.containsKey(name))
                        value = amzHeaders.get(name) + "," + header.getValue();
                    else value = header.getValue();

                    // All newlines and multiple spaces must be replaced with a
                    // single space character.
                    Matcher m = spacePattern.matcher(value);
                    value = m.replaceAll(" ");

                    amzHeaders.put(name, value);
                }
            }
        }

        // Concatenate all AMZ headers
        for (Entry<String, String> entry : amzHeaders.entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     *
     * @param reference The resource reference
     * @return The canonicalized resource name.
     */
    public static String getCanonicalizedResourceName(Reference reference) {
        String hostName = reference.getHostDomain();
        String path = reference.getPath();
        Pattern hostNamePattern = Pattern.compile("s3[a-z0-1\\-]*.amazonaws.com");
        StringBuilder sb = new StringBuilder();

        // Append the bucket
        if (hostName != null) {
            // If the host name contains a port number, remove it
            if (hostName.contains(":")) hostName = hostName.substring(0, hostName.indexOf(':'));

            Matcher hostNameMatcher = hostNamePattern.matcher(hostName);
            if (hostName.endsWith(".s3.amazonaws.com")) {
                String bucketName = hostName.substring(0, hostName.length() - 17);
                sb.append("/").append(bucketName);
            } else if (!hostNameMatcher.matches()) {
                sb.append("/").append(hostName);
            }
        }

        int queryIdx = path.indexOf('?');

        // Append the resource path
        if (queryIdx >= 0) sb.append(path, 0, queryIdx);
        else sb.append(path);

        // Append the AWS sub-resource
        if (queryIdx >= 0) {
            String query = path.substring(queryIdx - 1);

            if (query.contains("?acl")) sb.append("?acl");
            else if (query.contains("?location")) sb.append("?location");
            else if (query.contains("?logging")) sb.append("?logging");
            else if (query.contains("?torrent")) sb.append("?torrent");
        }

        return sb.toString();
    }

    /**
     * Returns the AWS authentication compatible signature for the given string to sign and secret.
     *
     * @param stringToSign The string to sign.
     * @param secret The user secret to sign with
     * @return The AWS compatible signature
     */
    public static String getHmacSha1Signature(String stringToSign, char[] secret) {
        return Base64.getEncoder()
                .encodeToString(DigestUtils.toHMacSha1(stringToSign, IoUtils.toByteArray(secret)));
    }

    /**
     * Returns the AWS authentication compatible signature for the given string to sign and secret.
     *
     * @param stringToSign The string to sign.
     * @param secret The user secret to sign with
     * @return The AWS compatible signature
     */
    public static String getHmacSha256Signature(String stringToSign, char[] secret) {
        return Base64.getEncoder()
                .encodeToString(
                        DigestUtils.toHMacSha256(stringToSign, IoUtils.toByteArray(secret)));
    }

    /**
     * Returns the AWS SimpleDB authentication compatible signature for the given request and
     * secret.
     *
     * @param method The request method.
     * @param resourceRef The target resource reference.
     * @param params The request parameters.
     * @param secret The user secret to sign with
     * @return The AWS SimpleDB compatible signature
     */
    public static String getQuerySignature(
            Method method, Reference resourceRef, List<Parameter> params, char[] secret) {
        return getHmacSha256Signature(getQueryStringToSign(method, resourceRef, params), secret);
    }

    /**
     * Returns the SimpleDB string to sign.
     *
     * @param resourceRef The target resource reference.
     * @return The string to sign.
     */
    public static String getQueryStringToSign(
            Method method, Reference resourceRef, List<Parameter> params) {
        StringBuilder toSign = new StringBuilder();

        // Append HTTP method
        toSign.append(method != null ? method.getName() : "").append("\n");

        // Append domain name
        String domain = resourceRef.getHostDomain();
        toSign.append(domain != null ? domain : "").append("\n");

        // Append URI path
        String path = resourceRef.getPath();
        toSign.append(path != null ? path : "").append("\n");

        // Prepare the query parameters
        Collections.sort(params);
        Parameter param;

        for (int i = 0; i < params.size(); i++) {
            param = params.get(i);

            if (i > 0) {
                toSign.append('&');
            }

            toSign.append(Reference.encode(param.getName()));

            if (param.getValue() != null) {
                toSign.append('=').append(Reference.encode(param.getValue(), true));
            }
        }

        return toSign.toString();
    }

    /**
     * Returns the AWS S3 authentication compatible signature for the given request and secret.
     *
     * @param request The request to create the signature for
     * @param secret The user secret to sign with
     * @return The AWS S3 compatible signature
     */
    public static String getS3Signature(Request request, char[] secret) {
        @SuppressWarnings("unchecked")
        Series<Header> headers =
                (Series<Header>) request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        return getS3Signature(request, headers, secret);
    }

    /**
     * Returns the AWS S3 authentication compatible signature for the given request and secret.
     *
     * @param request The request to create the signature for
     * @param headers The HTTP headers associated with the request
     * @param secret The user secret to sign with
     * @return The AWS S3 compatible signature
     */
    public static String getS3Signature(Request request, Series<Header> headers, char[] secret) {
        return getHmacSha1Signature(getS3StringToSign(request, headers), secret);
    }

    /**
     * Returns the string to sign.
     *
     * @param request The request to generate the signature string from
     * @return The string to sign
     */
    public static String getS3StringToSign(Request request) {
        @SuppressWarnings("unchecked")
        Series<Header> headers =
                (Series<Header>) request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        return getS3StringToSign(request, headers);
    }

    /**
     * Returns the S3 string to sign.
     *
     * @param request The request to generate the signature string from
     * @param headers The HTTP headers associated with the request
     * @return The string to sign
     */
    public static String getS3StringToSign(Request request, Series<Header> headers) {
        String canonicalizedAmzHeaders = getCanonicalizedAmzHeaders(headers);
        String canonicalizedResource = getCanonicalizedResourceName(request.getResourceRef());
        String method = request.getMethod().getName();
        String date = getDateAndInitHeaderIfNecessary(headers);
        String contentMD5 =
                (headers == null)
                        ? null
                        : headers.getFirstValue(HeaderConstants.HEADER_CONTENT_MD5, true);

        String contentType =
                (headers == null)
                        ? null
                        : headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE, true);
        if (isNullOrEmpty(contentType)
                && !request.getMethod().equals(Method.PUT)
                && SystemUtils.shouldApplyBug6331920Patch()) {
            contentType = "application/x-www-form-urlencoded";
        }

        return (method != null ? method : "")
                + "\n"
                + (contentMD5 != null ? contentMD5 : "")
                + "\n"
                + (contentType != null ? contentType : "")
                + "\n"
                + date
                + "\n"
                + canonicalizedAmzHeaders
                + canonicalizedResource;
    }

    private static String getDateAndInitHeaderIfNecessary(final Series<Header> headers) {
        String date = (headers == null) ? null : headers.getFirstValue("X-Amz-Date", true);
        // If amazon's date header wasn't found, try to grab the regular date header
        if (isNullOrEmpty(date)) {
            date =
                    (headers == null)
                            ? null
                            : headers.getFirstValue(HeaderConstants.HEADER_DATE, true);
        }

        // If no date header exists, make one
        if (isNullOrEmpty(date)) {
            date = DateUtils.format(new Date(), DateUtils.FORMAT_RFC_1123.getFirst());
            if (headers != null) {
                headers.add(HeaderConstants.HEADER_DATE, date);
            }
        }
        return date;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private AwsUtils() {
        /* This utility class should not be instantiated */
    }
}
