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
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Implements the Shared Key authentication for Azure services. This concerns Blob and Queues on
 * Azure Storage.<br>
 * <br>
 * More documentation is available <a
 * href="http://msdn.microsoft.com/en-us/library/dd179428.aspx">here</a>
 *
 * @author Thierry Boileau
 */
public class HttpAzureSharedKeyHelper extends AuthenticatorHelper {

    /**
     * Returns the canonicalized Azure headers.
     *
     * @param requestHeaders The list of request headers.
     * @return The canonicalized Azure headers.
     */
    private static String getCanonicalizedAzureHeaders(Series<Header> requestHeaders) {
        // Filter out all the Azure headers required for SharedKey
        // authentication
        SortedMap<String, String> azureHeaders = new TreeMap<>();
        String headerName;

        for (Header header : requestHeaders) {
            headerName = header.getName().toLowerCase();

            if (headerName.startsWith("x-ms-")) {
                azureHeaders.computeIfAbsent(headerName, requestHeaders::getValues);
            }
        }

        // Concatenate all Azure headers
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : azureHeaders.entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     *
     * @param resourceRef The resource reference.
     * @return The canonicalized resource name.
     */
    private static String getCanonicalizedResourceName(Reference resourceRef) {
        Form form = resourceRef.getQueryAsForm();
        Parameter param = form.getFirst("comp", true);

        if (param != null) {
            return resourceRef.getPath() + "?" + "comp=" + param.getValue();
        }

        return resourceRef.getPath();
    }

    /** Constructor. */
    public HttpAzureSharedKeyHelper() {
        super(ChallengeScheme.HTTP_AZURE_SHAREDKEY, true, false);
    }

    @Override
    public void formatResponse(
            ChallengeWriter cw,
            ChallengeResponse challenge,
            Request request,
            Series<Header> httpHeaders) {

        // Set up the message part
        final String rest =
                request.getMethod().getName()
                        + '\n'
                        + getContentMd5(httpHeaders)
                        + '\n'
                        + getContentTypeHeader(request, httpHeaders)
                        + '\n'
                        + getDateHeader(httpHeaders)
                        + '\n'
                        + getCanonicalizedAzureHeaders(httpHeaders)
                        + '/'
                        + challenge.getIdentifier()
                        + getCanonicalizedResourceName(request.getResourceRef());

        // Append the SharedKey credentials
        cw.append(challenge.getIdentifier())
                .append(':')
                .append(
                        Base64.getEncoder()
                                .encodeToString(
                                        DigestUtils.toHMacSha256(
                                                rest,
                                                Base64.getDecoder()
                                                        .decode(
                                                                IoUtils.toByteArray(
                                                                        challenge.getSecret())))));
    }

    private static String getContentMd5(final Series<Header> httpHeaders) {
        String contentMd5 = httpHeaders.getFirstValue(HeaderConstants.HEADER_CONTENT_MD5, true);
        if (contentMd5 == null) {
            contentMd5 = "";
        }
        return contentMd5;
    }

    private static String getContentTypeHeader(
            final Request request, final Series<Header> httpHeaders) {
        String contentType = httpHeaders.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE, true);

        if (contentType != null) {
            return contentType;
        }

        if (!request.getMethod().equals(Method.PUT) && SystemUtils.shouldApplyBug6331920Patch()) {
            contentType = "application/x-www-form-urlencoded";
        } else {
            contentType = "";
        }

        return contentType;
    }

    private static String getDateHeader(final Series<Header> httpHeaders) {
        String date = "";

        if (httpHeaders.getFirstValue("x-ms-date", true) == null) {
            // X-ms-Date header didn't override the standard Date header
            date = httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true);
            if (date == null) {
                // Add a fresh Date header
                date = DateUtils.format(new Date(), DateUtils.FORMAT_RFC_1123.getFirst());
                httpHeaders.add(HeaderConstants.HEADER_DATE, date);
            }
        }
        return date;
    }
}
