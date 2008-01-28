/*
 * Copyright 2005-2008 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs;

import java.security.Principal;

/**
 * An Authenticator that forbid every what it is requested.
 * @see Authenticator
 * @see AllowAllAuthenticator
 * @see ThrowExcAuthenticator
 * @author Stephan Koops
 */
public class ForbidAllAuthenticator implements Authenticator {

    private static ForbidAllAuthenticator instance;
    
    /**
     * Returns an instance of the AllowAllAuthenticator
     * @return the singelton instance.
     */
    public static ForbidAllAuthenticator getInstance()
    {
        if(instance == null)
            instance = new ForbidAllAuthenticator();
        return instance;
    }
    
    /**
     * @see org.restlet.ext.jaxrs.Authenticator#isUserInRole(Principal, java.lang.String)
     */
    public boolean isUserInRole(Principal principal, String role) {
        return false;
    }

    /**
     * @see org.restlet.ext.jaxrs.Authenticator#checkSecret(String, char[])
     */
    public boolean checkSecret(String identifier, char[] secret) {
        return false;
    }
}