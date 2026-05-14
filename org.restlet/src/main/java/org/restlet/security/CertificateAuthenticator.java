/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * Authenticator based on the SSL client certificate. If a client certificate is presented, and
 * accepted by your SSL certificate truststore, it adds the Principal of its subject to the list of
 * principals in the request's ClientInfo. It also sets the user to be a new User based on this
 * Principal.
 *
 * <p>{@link #getPrincipals(List)} and {@link #getUser(Principal)} can be overridden to change the
 * default behavior.
 *
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 */
public class CertificateAuthenticator extends Authenticator {

    /**
     * @param context
     */
    public CertificateAuthenticator(Context context) {
        super(context);
    }

    /**
     * Extracts the Principal of the subject to use from a chain of certificate. By default, this is
     * the X500Principal of the subject of the first certificate in the chain.
     *
     * @see X509Certificate
     * @see X500Principal
     * @param certificateChain chain of client certificates.
     * @return Principal of the client certificate or null if the chain is empty.
     */
    protected List<Principal> getPrincipals(List<Certificate> certificateChain) {
        ArrayList<Principal> principals = null;

        if ((certificateChain != null) && (!certificateChain.isEmpty())) {
            Certificate userCert = certificateChain.getFirst();

            if (userCert instanceof X509Certificate x509Certificate) {
                principals = new ArrayList<>();
                principals.add(x509Certificate.getSubjectX500Principal());
            }

            return principals;
        } else {
            return null;
        }
    }

    /**
     * Creates a new User based on the subject's X500Principal. By default, the username is the
     * subject distinguished name, formatted according to RFC 2253. Some may choose to extract the
     * Common Name only, for example.
     *
     * @param principal subject's Principal (most likely X500Principal).
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
     * Authenticates the call using the X.509 client certificate. The SSL layer normally does the
     * verification of the credentials, via the TrustManagers.
     *
     * <p>It uses the certificate chain in the request's "org.restlet.https.clientCertificates"
     * attribute, adds the principal returned from this chain by {@link #getPrincipals(List)} to the
     * request's ClientInfo, and set the user to the result of {@link #getUser(Principal)} if that
     * user is non-null.
     *
     * <p>If no client certificate is available, then a 401 status is set.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        List<Certificate> certChain = request.getClientInfo().getCertificates();
        List<Principal> principals = getPrincipals(certChain);

        if ((principals != null) && (!principals.isEmpty())) {
            request.getClientInfo().getPrincipals().addAll(principals);
            User user = getUser(principals.getFirst());

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
