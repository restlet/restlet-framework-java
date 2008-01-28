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
 * @see Authorizator
 * @see ForbidAllAuthorizator
 * @author Stephan Koops
 */
public class AllowAllAuthorizator implements Authorizator {
    
    private static AllowAllAuthorizator instance;
    
    /**
     * Returns an instance of the AllowAllAuthorizator
     * @return the singelton instance.
     */
    public static AllowAllAuthorizator getInstance()
    {
        if(instance == null)
            instance = new AllowAllAuthorizator();
        return instance;
    }
    
    /**
     * @see org.restlet.ext.jaxrs.Authorizator#isUserInRole(Principal, java.lang.String)
     */
    public boolean isUserInRole(Principal principal, String role) {
        return true;
    }

    /**
     * @see org.restlet.ext.jaxrs.Authorizator#checkSecret(String, char[])
     */
    public boolean checkSecret(String identifier, char[] secret) {
        return true;
    }
}