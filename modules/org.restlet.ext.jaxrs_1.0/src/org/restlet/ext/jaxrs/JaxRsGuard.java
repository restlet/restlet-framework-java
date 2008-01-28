/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs;

import java.security.Principal;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.core.PrincipalImpl;
import org.restlet.ext.jaxrs.util.Util;

/**
 * This JaxRsGuard is used to check credentials and roles for a JaxRsRouter.
 * 
 * @see JaxRsRouter#getGuarded(Context, ChallengeScheme, String, Authenticator)
 * @author Stephan Koops
 */
public class JaxRsGuard extends org.restlet.Guard {

    private Authenticator authenticator;

    /**
     * @param context
     * @param scheme
     * @param realm
     * @param authenticator
     *                must not be null
     * @throws IllegalArgumentException
     *                 if the authenticator is null.
     */
    public JaxRsGuard(Context context, ChallengeScheme scheme, String realm,
            Authenticator authenticator) throws IllegalArgumentException {
        super(context, scheme, realm);
        if (authenticator == null)
            throw new IllegalArgumentException(
                    "The Authenticator must not be null. You can use the "
                            + AllowAllAuthenticator.class.getName()
                            + " or the "
                            + ForbidAllAuthenticator.class.getName());
        this.authenticator = authenticator;
    }

    /**
     * Indicates if the secret is valid for the given identifier. By default,
     * this returns true given the correct login/password couple as verified via
     * the findSecret() method.
     * 
     * @param identifier
     *                the identifier
     * @param secret
     *                the identifier's secret
     * @return true if the secret is valid for the given identifier
     */
    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        return this.authenticator.checkSecret(identifier, secret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Request request, Response response) {
        Principal principal = null;
        ChallengeResponse challengeResponse = request.getChallengeResponse();
        if (challengeResponse != null) {
            String credentials = challengeResponse.getIdentifier();
            if (credentials != null)
                principal = new PrincipalImpl(credentials);
        }
        Util.setPrincipal(principal, request);
        super.accept(request, response);
    }

    /**
     * @see Guard#authenticate(Request)
     */
    @Override
    public int authenticate(Request request) {
        int result = super.authenticate(request);
        if (result == 0) // no credetinals found
        { // check if no-credentials is perhaps valid
            // @see AllowAllAuthenticator
            try {
                boolean check = this.authenticator.checkSecret(null, null);
                if (check)
                    return 1; // credentials are ok.
            } catch (RuntimeException e) {
                // than ignore it and continue the default way
            }
        }
        return result;
    }

    /**
     * Attatches the given root resource class
     * 
     * @param jaxRsClass
     *                The root resource class
     * @see #attach(Class)
     */
    public void attach(Class<?> jaxRsClass) {
        getNext().attach(jaxRsClass);
    }

    /**
     * Detaches the JAX-RS root resource class from this router.
     * 
     * @param jaxRsClass
     *                The JAX-RS root resource class to detach.
     * @see #attach(Class)
     */
    public void detach(Class<?> jaxRsClass) {
        getNext().detach(jaxRsClass);
    }

    @Override
    public JaxRsRouter getNext() {
        return (JaxRsRouter) super.getNext();
    }

    /**
     * You should use other {@link #checkSecret(Request, String, char[])}
     * method.
     */
    @Deprecated
    @Override
    protected boolean checkSecret(String identifier, char[] secret) {
        return this.authenticator.checkSecret(identifier, secret);
    }

    @Deprecated
    @Override
    protected char[] findSecret(String identifier) {
        throw new UnsupportedOperationException(
                "This method is not needed and forbidden in this subclass");
    }

    @Deprecated
    @Override
    public ConcurrentMap<String, char[]> getSecrets() {
        throw new UnsupportedOperationException(
                "This method is not needed and forbidden in this subclass");
    }
}
