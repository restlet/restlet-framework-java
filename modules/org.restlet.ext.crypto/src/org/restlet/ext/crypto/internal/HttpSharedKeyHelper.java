/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.crypto.internal;

import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.SystemUtils;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.util.Series;

/**
 * Implements the Shared Key authentication for Azure services. This concerns
 * Blob and Queues on Azure Storage.<br>
 * <br>
 * More documentation is available <a
 * href="http://msdn.microsoft.com/en-us/library/dd179428.aspx">here</a>
 * 
 * @author Thierry Boileau
 */
public class HttpSharedKeyHelper extends AuthenticatorHelper {

    /**
     * Returns the canonicalized Azure headers.
     * 
     * @param requestHeaders
     *            The list of request headers.
     * @return The canonicalized Azure headers.
     */
    private static String getCanonicalizedAzureHeaders(
            Series<Parameter> requestHeaders) {
        // Filter out all the Azure headers required for SharedKey
        // authentication
        final SortedMap<String, String> azureHeaders = new TreeMap<String, String>();
        String headerName;
        for (final Parameter param : requestHeaders) {
            headerName = param.getName().toLowerCase();
            if (headerName.startsWith("x-ms-")) {
                if (!azureHeaders.containsKey(headerName)) {
                    azureHeaders.put(headerName, requestHeaders
                            .getValues(headerName));
                }
            }
        }

        // Concatenate all Azure headers
        final StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = azureHeaders.keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            sb.append(key).append(':').append(azureHeaders.get(key)).append(
                    "\n");
        }

        return sb.toString();
    }

    /**
     * Returns the canonicalized resource name.
     * 
     * @param resourceRef
     *            The resource reference.
     * @return The canonicalized resource name.
     */
    private static String getCanonicalizedResourceName(Reference resourceRef) {
        Form form = resourceRef.getQueryAsForm();

        Parameter param = form.getFirst("comp", true);
        if (param != null) {
            StringBuilder sb = new StringBuilder(resourceRef.getPath());
            return sb.append("?").append("comp=").append(param.getValue())
                    .toString();
        }
        return resourceRef.getPath();
    }

    /**
     * Constructor.
     */
    public HttpSharedKeyHelper() {
        super(ChallengeScheme.HTTP_AZURE_SHAREDKEY, true, false);
    }

    @Override
    public void formatRawResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {

        // Setup the method name
        final String methodName = request.getMethod().getName();

        // Setup the Date header
        String date = "";

        if (httpHeaders.getFirstValue("x-ms-date", true) == null) {
            // X-ms-Date header didn't override the standard Date header
            date = httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true);
            if (date == null) {
                // Add a fresh Date header
                date = DateUtils.format(new Date(), DateUtils.FORMAT_RFC_1123
                        .get(0));
                httpHeaders.add(HeaderConstants.HEADER_DATE, date);
            }
        }
        // Setup the ContentType header
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

        // Setup the canonicalized AzureHeaders
        final String canonicalizedAzureHeaders = getCanonicalizedAzureHeaders(httpHeaders);

        // Setup the canonicalized path
        final String canonicalizedResource = getCanonicalizedResourceName(request
                .getResourceRef());

        // Setup the message part
        final StringBuilder rest = new StringBuilder();
        rest.append(methodName).append('\n').append(contentMd5).append('\n')
                .append(contentType).append('\n').append(date).append('\n')
                .append(canonicalizedAzureHeaders).append('/').append(
                        challenge.getIdentifier())
                .append(canonicalizedResource);

        // Append the SharedKey credentials
        cw.append(challenge.getIdentifier()).append(':').append(
                Base64.encode(DigestUtils.toHMac256(rest.toString(), Base64
                        .decode(challenge.getSecret())), true));
    }
}
