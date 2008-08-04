/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.util;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;

import org.restlet.data.Request;

/**
 * @author Stephan
 * 
 */
public class SecurityUtil {

    /** Key in the request attributes for the https client certificates. */
    private static final String ORG_RESTLET_HTTPS_CLIENT_CERTS = "org.restlet.https.clientCertificates";

    /**
     * Returns the Principal from the SSl client certificates (the first with a
     * name).
     * 
     * @param request
     *            the Request to get the Principal from.
     * @return the Principal, or null, if no one is found.
     */
    public static Principal getSslClientCertPrincipal(Request request) {
        final List<X509Certificate> sslClientCerts = getSslClientCerts(request);
        if (sslClientCerts != null) {
            for (final X509Certificate cert : sslClientCerts) {
                final Principal p = cert.getSubjectDN();
                if ((p.getName() != null) && (p.getName().length() > 0)) {
                    return p;
                }
            }
        }
        return null;
    }

    // LATER load auth data from Servlet-API ?

    /**
     * Returns the client certificates from the given Request.
     * 
     * @param request
     *            The request to get the client certificates from
     * @return the client certifucates. May be null.
     */
    @SuppressWarnings("unchecked")
    private static List<X509Certificate> getSslClientCerts(Request request) {
        return (List<X509Certificate>) request.getAttributes().get(
                ORG_RESTLET_HTTPS_CLIENT_CERTS);
    }

    /**
     * Checks, if the given request was authenticated by a SSL client
     * certificate.
     * 
     * @param request
     *            The Request to check
     * @return true, if the given request was authenticated by a SSL client
     *         certificate, otherwise false.
     */
    public static boolean isSslClientCertAuth(Request request) {
        return getSslClientCerts(request) != null;
    }
}