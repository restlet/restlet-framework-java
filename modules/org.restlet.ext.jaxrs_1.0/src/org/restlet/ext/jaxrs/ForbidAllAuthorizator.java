package org.restlet.ext.jaxrs;

import java.security.Principal;

/**
 * An Authorizator that forbid every what it is requested.
 * @see Authorizator
 * @see AllowAllAuthorizator
 * @author Stephan Koops
 */
public class ForbidAllAuthorizator implements Authorizator {

    private static ForbidAllAuthorizator instance;
    
    /**
     * Returns an instance of the AllowAllAuthorizator
     * @return the singelton instance.
     */
    public static ForbidAllAuthorizator getInstance()
    {
        if(instance == null)
            instance = new ForbidAllAuthorizator();
        return instance;
    }
    
    /**
     * @see org.restlet.ext.jaxrs.Authorizator#isUserInRole(Principal, java.lang.String)
     */
    public boolean isUserInRole(Principal principal, String role) {
        return false;
    }

    /**
     * @see org.restlet.ext.jaxrs.Authorizator#checkSecret(String, char[])
     */
    public boolean checkSecret(String identifier, char[] secret) {
        return false;
    }
}