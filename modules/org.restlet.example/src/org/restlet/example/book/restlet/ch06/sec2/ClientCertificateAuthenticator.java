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
package org.restlet.example.book.restlet.ch06.sec2;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import org.restlet.security.User;

/**
 * Authenticator based on the SSL client certificate. If a client certificate is
 * presented, it adds the Principal of its subject to the list of principals in
 * the request's ClientInfo. It also sets the user to be a new User based on
 * this Principal.
 * 
 * {@link #getPrincipal(List)} and {@link #getUser(Principal)} can be overridden
 * to change the default behaviour.
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class ClientCertificateAuthenticator extends Authenticator {
    public ClientCertificateAuthenticator(Context context) {
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
    protected List<Principal> getPrincipals(
            List<X509Certificate> certificateChain) {
        if ((certificateChain != null) && (certificateChain.size() > 0)) {
            ArrayList<Principal> principals = new ArrayList<Principal>();
            X509Certificate userCert = certificateChain.get(0);
            principals.add(userCert.getSubjectX500Principal());
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
     * returned from this chain by {@link #getPrincipal(List)} to the request's
     * ClientInfo and set the user to the result of {@link #getUser(Principal)}
     * if that user is non-null.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        @SuppressWarnings("unchecked")
        List<X509Certificate> certchain = (List<X509Certificate>) request
                .getAttributes().get("org.restlet.https.clientCertificates");

        List<Principal> principals = getPrincipals(certchain);

        if ((principals != null) && (principals.size() > 0)) {
            request.getClientInfo().getPrincipals().addAll(principals);
            User user = getUser(principals.get(0));
            if (user != null) {
                request.getClientInfo().setUser(user);
            }
            return true;
        } else {
            return false;
        }
    }
}
