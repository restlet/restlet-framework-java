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

package org.restlet.security;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * Authenticator based on the SSL client certificate. If a client certificate is
 * presented, and accepted by your SSL certificate truststore, it adds the
 * Principal of its subject to the list of principals in the request's
 * ClientInfo. It also sets the user to be a new User based on this Principal.
 * 
 * {@link #getPrincipals(List)} and {@link #getUser(Principal)} can be
 * overridden to change the default behavior.
 * 
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 */
public class CertificateAuthenticator extends Authenticator {

    /**
     * 
     * @param context
     */
    public CertificateAuthenticator(Context context) {
        super(context);
    }

    /**
     * Extracts the Principal of the subject to use from a chain of certificate.
     * By default, this is the X500Principal of the subject subject of the first
     * certificate in the chain.
     * 
     * @see X509Certificate
     * @see X500Principal
     * @param certificateChain
     *            chain of client certificates.
     * @return Principal of the client certificate or null if the chain is
     *         empty.
     */
    protected List<Principal> getPrincipals(List<Certificate> certificateChain) {
        ArrayList<Principal> principals = null;

        if ((certificateChain != null) && (certificateChain.size() > 0)) {
            Certificate userCert = certificateChain.get(0);

            if (userCert instanceof X509Certificate) {
                principals = new ArrayList<Principal>();
                principals.add(((X509Certificate) userCert)
                        .getSubjectX500Principal());
            }

            return principals;
        } else {
            return null;
        }
    }

    /**
     * Creates a new User based on the subject's X500Principal. By default, the
     * user name is the subject distinguished name, formatted accorded to RFC
     * 2253. Some may choose to extract the Common Name only, for example.
     * 
     * @param principal
     *            subject's Principal (most likely X500Principal).
     * @return User instance corresponding to this principal or null.
     */
    protected User getUser(Principal principal) {
        if (principal != null) {
            return new User(principal.getName());
        } else {
            return null;
        }
    }

    /**
     * Authenticates the call using the X.509 client certificate. The
     * verification of the credentials is normally done by the SSL layer, via
     * the TrustManagers.
     * 
     * It uses the certificate chain in the request's
     * "org.restlet.https.clientCertificates" attribute, adds the principal
     * returned from this chain by {@link #getPrincipals(List)} to the request's
     * ClientInfo and set the user to the result of {@link #getUser(Principal)}
     * if that user is non-null.
     * 
     * If no client certificate is available, then a 401 status is set.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        List<Certificate> certchain = request.getClientInfo().getCertificates();
        List<Principal> principals = getPrincipals(certchain);

        if ((principals != null) && (principals.size() > 0)) {
            request.getClientInfo().getPrincipals().addAll(principals);
            User user = getUser(principals.get(0));

            if (user != null) {
                request.getClientInfo().setUser(user);
            }
            return true;
        } else {
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return false;
        }
    }

}
