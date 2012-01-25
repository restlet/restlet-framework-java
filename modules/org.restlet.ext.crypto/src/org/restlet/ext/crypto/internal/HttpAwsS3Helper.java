/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto.internal;

import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Implements the HTTP authentication for the Amazon S3 service.
 * 
 * @author Jerome Louvel
 */
public class HttpAwsS3Helper extends AuthenticatorHelper {

    /**
     * Returns the canonicalized AMZ headers.
     * 
     * @param requestHeaders
     *            The list of request headers.
     * @return The canonicalized AMZ headers.
     */
    private static String getCanonicalizedAmzHeaders(
            Series<Parameter> requestHeaders) {
        // Filter out all the AMZ headers required for AWS authentication
        final SortedMap<String, String> amzHeaders = new TreeMap<String, String>();
        String headerName;
        for (final Parameter param : requestHeaders) {
            headerName = param.getName().toLowerCase();
            if (headerName.startsWith("x-amz-")) {
                if (!amzHeaders.containsKey(headerName)) {
                    amzHeaders.put(headerName,
                            requestHeaders.getValues(headerName));
                }
            }
        }

        // Concatenate all AMZ headers
        final StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = amzHeaders.keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            sb.append(key).append(':').append(amzHeaders.get(key)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     * 
     * @param reference
     *            The resource reference.
     * @return The canonicalized resource name.
     */
    private static String getCanonicalizedResourceName(Reference reference) {
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
     * Constructor.
     */
    public HttpAwsS3Helper() {
        super(ChallengeScheme.HTTP_AWS_S3, true, false);
    }

    @Override
    public void formatRawResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {

        // Setup the method name
        final String methodName = request.getMethod().getName();

        // Setup the Date header
        String date = "";

        if (httpHeaders.getFirstValue("X-Amz-Date", true) == null) {
            // X-Amz-Date header didn't override the standard Date header
            date = httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true);
            if (date == null) {
                // Add a fresh Date header
                date = DateUtils.format(new Date(),
                        DateUtils.FORMAT_RFC_1123.get(0));
                httpHeaders.add(HeaderConstants.HEADER_DATE, date);
            }
        }

        // Setup the Content-MD5 header
        String contentMd5 = httpHeaders.getFirstValue(
                HeaderConstants.HEADER_CONTENT_MD5, true);
        if (contentMd5 == null) {
            contentMd5 = "";
        }

        // Setup the ContentType header
        String contentType = httpHeaders.getFirstValue(
                HeaderConstants.HEADER_CONTENT_TYPE, true);
        if (contentType == null) {
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

            if (applyPatch && !request.getMethod().equals(Method.PUT)) {
                contentType = "application/x-www-form-urlencoded";
            } else {
                contentType = "";
            }
        }

        // Setup the canonicalized AmzHeaders
        final String canonicalizedAmzHeaders = getCanonicalizedAmzHeaders(httpHeaders);

        // Setup the canonicalized resource name
        final String canonicalizedResource = getCanonicalizedResourceName(request
                .getResourceRef());

        // Setup the message part
        final StringBuilder rest = new StringBuilder();
        rest.append(methodName).append('\n').append(contentMd5).append('\n')
                .append(contentType).append('\n').append(date).append('\n')
                .append(canonicalizedAmzHeaders).append(canonicalizedResource);

        // Append the AWS credentials
        cw.append(challenge.getIdentifier())
                .append(':')
                .append(Base64.encode(
                        DigestUtils.toHMac(rest.toString(),
                                BioUtils.toByteArray(challenge.getSecret())),
                        false));

    }

}
