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
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Provides utility functions for implementing the Amazon S3 Authentication
 * scheme.
 * 
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 * @see <a
 *      href="http://docs.amazonwebservices.com/AmazonS3/latest/RESTAuthentication.html">
 *      Authenticating REST Requests</a>
 */
public class AwsUtils {

    /**
     * Returns the canonicalized AMZ headers.
     * 
     * @param requestHeaders
     *            The list of request headers.
     * @return The canonicalized AMZ headers.
     */
    public static String getCanonicalizedAmzHeaders(
            Series<Header> requestHeaders) {
        StringBuilder sb = new StringBuilder();
        Pattern spacePattern = Pattern.compile("\\s+");

        // Create a lexographically sorted list of headers that begin with x-amz
        SortedMap<String, String> amzHeaders = new TreeMap<String, String>();

        if (requestHeaders != null) {
            for (Header header : requestHeaders) {
                String name = header.getName().toLowerCase();

                if (name.startsWith("x-amz-")) {
                    String value = "";

                    if (amzHeaders.containsKey(name))
                        value = amzHeaders.get(name) + "," + header.getValue();
                    else
                        value = header.getValue();

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
            sb.append(entry.getKey()).append(':').append(entry.getValue())
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     * 
     * @param reference
     *            The resource reference
     * @return The canonicalized resource name.
     */
    public static String getCanonicalizedResourceName(Reference reference) {
        String hostName = reference.getHostDomain();
        String path = reference.getPath();
        Pattern hostNamePattern = Pattern
                .compile("s3[a-z0-1\\-]*.amazonaws.com");
        StringBuilder sb = new StringBuilder();

        // Append the bucket
        if (hostName != null) {
            // If the host name contains a port number remove it
            if (hostName.contains(":"))
                hostName = hostName.substring(0, hostName.indexOf(":"));

            Matcher hostNameMatcher = hostNamePattern.matcher(hostName);
            if (hostName.endsWith(".s3.amazonaws.com")) {
                String bucketName = hostName.substring(0,
                        hostName.length() - 17);
                sb.append("/" + bucketName);
            } else if (!hostNameMatcher.matches()) {
                sb.append("/" + hostName);
            }
        }

        int queryIdx = path.indexOf("?");

        // Append the resource path
        if (queryIdx >= 0)
            sb.append(path.substring(0, queryIdx));
        else
            sb.append(path.substring(0, path.length()));

        // Append the AWS sub-resource
        if (queryIdx >= 0) {
            String query = path.substring(queryIdx - 1, path.length());

            if (query.contains("?acl"))
                sb.append("?acl");
            else if (query.contains("?location"))
                sb.append("?location");
            else if (query.contains("?logging"))
                sb.append("?logging");
            else if (query.contains("?torrent"))
                sb.append("?torrent");
        }

        return sb.toString();
    }

    /**
     * Returns the AWS authentication compatible signature for the given string
     * to sign and secret.
     * 
     * @param stringToSign
     *            The string to sign.
     * @param secret
     *            The user secret to sign with
     * @return The AWS compatible signature
     */
    public static String getHmacSha1Signature(String stringToSign, char[] secret) {
        return Base64.encode(
                DigestUtils.toHMacSha1(stringToSign,
                        IoUtils.toByteArray(secret)), false);
    }

    /**
     * Returns the AWS authentication compatible signature for the given string
     * to sign and secret.
     * 
     * @param stringToSign
     *            The string to sign.
     * @param secret
     *            The user secret to sign with
     * @return The AWS compatible signature
     */
    public static String getHmacSha256Signature(String stringToSign,
            char[] secret) {
        return Base64.encode(
                DigestUtils.toHMacSha256(stringToSign,
                        IoUtils.toByteArray(secret)), false);
    }

    /**
     * Returns the AWS SimpleDB authentication compatible signature for the
     * given request and secret.
     * 
     * @param method
     *            The request method.
     * @param resourceRef
     *            The target resource reference.
     * @param params
     *            The request parameters.
     * @param secret
     *            The user secret to sign with
     * @return The AWS SimpleDB compatible signature
     */
    public static String getQuerySignature(Method method,
            Reference resourceRef, List<Parameter> params, char[] secret) {
        return getHmacSha256Signature(
                getQueryStringToSign(method, resourceRef, params), secret);
    }

    /**
     * Returns the SimpleDB string to sign.
     * 
     * @param resourceRef
     *            The target resource reference.
     * @return The string to sign.
     */
    public static String getQueryStringToSign(Method method,
            Reference resourceRef, List<Parameter> params) {
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
                toSign.append('=').append(
                        Reference.encode(param.getValue(), true));
            }
        }

        return toSign.toString();
    }

    /**
     * Returns the AWS S3 authentication compatible signature for the given
     * request and secret.
     * 
     * @param request
     *            The request to create the signature for
     * @param secret
     *            The user secret to sign with
     * @return The AWS S3 compatible signature
     */
    public static String getS3Signature(Request request, char[] secret) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) request.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        return getS3Signature(request, headers, secret);
    }

    /**
     * Returns the AWS S3 authentication compatible signature for the given
     * request and secret.
     * 
     * @param request
     *            The request to create the signature for
     * @param headers
     *            The HTTP headers associated with the request
     * @param secret
     *            The user secret to sign with
     * @return The AWS S3 compatible signature
     */
    public static String getS3Signature(Request request,
            Series<Header> headers, char[] secret) {
        return getHmacSha1Signature(getS3StringToSign(request, headers), secret);
    }

    /**
     * Returns the string to sign.
     * 
     * @param request
     *            The request to generate the signature string from
     * @return The string to sign
     */
    public static String getS3StringToSign(Request request) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) request.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        return getS3StringToSign(request, headers);
    }

    /**
     * Returns the S3 string to sign.
     * 
     * @param request
     *            The request to generate the signature string from
     * @param headers
     *            The HTTP headers associated with the request
     * @return The string to sign
     */
    public static String getS3StringToSign(Request request,
            Series<Header> headers) {
        String canonicalizedAmzHeaders = getCanonicalizedAmzHeaders(headers);
        String canonicalizedResource = getCanonicalizedResourceName(request
                .getResourceRef());
        String contentMD5 = (headers == null) ? null : headers.getFirstValue(
                HeaderConstants.HEADER_CONTENT_MD5, true);
        String contentType = (headers == null) ? null : headers.getFirstValue(
                HeaderConstants.HEADER_CONTENT_TYPE, true);
        String date = (headers == null) ? null : headers.getFirstValue(
                "X-Amz-Date", true);
        String method = request.getMethod().getName();

        // If amazon's date header wasn't found try to grab the regular date
        // header
        if (date == null || (date.length() == 0)) {
            date = (headers == null) ? null : headers.getFirstValue(
                    HeaderConstants.HEADER_DATE, true);
        }

        // If no date header exists make one
        if (date == null || (date.length() == 0)) {
            date = DateUtils.format(new Date(),
                    DateUtils.FORMAT_RFC_1123.get(0));
            if (headers != null) {
                headers.add(HeaderConstants.HEADER_DATE, date);
            }
        }

        if (contentType == null || (contentType.length() == 0)) {
            boolean applyPatch = false;

            // This patch seems to apply to Sun JVM only.
            final String jvmVendor = System.getProperty("java.vm.vendor");
            if ((jvmVendor != null)
                    && (jvmVendor.toLowerCase()).startsWith("sun")) {
                final int majorVersionNumber = SystemUtils
                        .getJavaMajorVersion();
                final int minorVersionNumber = SystemUtils
                        .getJavaMinorVersion();

                if (majorVersionNumber == 1) {
                    if (minorVersionNumber < 5) {
                        applyPatch = true;
                    } else if (minorVersionNumber == 5) {
                        // Sun fixed the bug in update 10
                        applyPatch = (SystemUtils.getJavaUpdateVersion() < 10);
                    }
                }
            }

            if (applyPatch && !request.getMethod().equals(Method.PUT))
                contentType = "application/x-www-form-urlencoded";
        }

        StringBuilder toSign = new StringBuilder();
        toSign.append(method != null ? method : "").append("\n");
        toSign.append(contentMD5 != null ? contentMD5 : "").append("\n");
        toSign.append(contentType != null ? contentType : "").append("\n");
        toSign.append(date != null ? date : "").append("\n");
        toSign.append(canonicalizedAmzHeaders != null ? canonicalizedAmzHeaders
                : "");
        toSign.append(canonicalizedResource != null ? canonicalizedResource
                : "");

        return toSign.toString();
    }
}
