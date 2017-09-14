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

package org.restlet.ext.jaxrs.internal.util;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;

import org.restlet.Request;

/**
 * Security utilities.
 * 
 * @author Stephan Koops
 */
public class SecurityUtil {

    /** Key in the request attributes for the HTTPS client certificates. */
    private static final String ORG_RESTLET_HTTPS_CLIENT_CERTS = "org.restlet.https.clientCertificates";

    /**
     * Returns the Principal from the SSL client certificates (the first with a
     * name).
     * 
     * @param request
     *            The Request to get the Principal from.
     * @return The Principal, or null, if no one is found.
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
