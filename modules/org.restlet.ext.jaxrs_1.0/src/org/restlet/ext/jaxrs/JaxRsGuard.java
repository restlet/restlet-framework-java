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

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

/**
 * This JaxRsGuard is used to check credentials and roles for a JaxRsRouter.
 * 
 * @see JaxRsRouter#getGuarded(Context, ChallengeScheme, String, Authorizator)
 * @author Stephan Koops
 */
public class JaxRsGuard extends org.restlet.Guard {

    private Authorizator authorizator;

    /**
     * @param context
     * @param scheme
     * @param realm
     * @param authorizator must not be null
     * @throws IllegalArgumentException
     *                 if the authorizator is null.
     */
    public JaxRsGuard(Context context, ChallengeScheme scheme, String realm,
            Authorizator authorizator) throws IllegalArgumentException {
        super(context, scheme, realm);
        if (authorizator == null)
            throw new IllegalArgumentException(
                    "The Authorizator must not be null. You can use the "
                            + AllowAllAuthorizator.class.getName() + " or the "
                            + ForbidAllAuthorizator.class.getName());
        this.authorizator = authorizator;
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
        return this.authorizator.checkSecret(identifier, secret);
    }

    /**
     * You should use other {@link #checkSecret(Request, String, char[])}
     * method.
     */
    @Deprecated
    @Override
    protected boolean checkSecret(String identifier, char[] secret) {
        return this.authorizator.checkSecret(identifier, secret);
    }

    @Override
    protected char[] findSecret(String identifier) {
        throw new UnsupportedOperationException(
                "This method is not needed and not forbidden in this subclass");
    }

    @Override
    public ConcurrentMap<String, char[]> getSecrets() {
        throw new UnsupportedOperationException(
                "This method is not needed and not forbidden in this subclass");
    }
}
