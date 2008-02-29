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

import javax.ws.rs.WebApplicationException;

import org.restlet.data.Status;

/**
 * An {@link AccessControl} that throws an WebApplicationExeption with status
 * 500 (Internal Server Error) for every call on it.
 * 
 * @see AccessControl
 * @see AllowAllAccess
 * @see ForbidAllAccess
 * @author Stephan Koops
 */
public class ThrowExcAccessControl implements AccessControl {

    private static ThrowExcAccessControl instance;

    /**
     * Returns an instance of the ThrowExcAccessControl
     * 
     * @return the singelton instance.
     */
    public static ThrowExcAccessControl getInstance() {
        if (instance == null)
            instance = new ThrowExcAccessControl();
        return instance;
    }

    /**
     * @see org.restlet.ext.jaxrs.AccessControl#isUserInRole(Principal,
     *      java.lang.String)
     */
    public boolean isUserInRole(Principal principal, String role) {
        throw new WebApplicationException(Status.SERVER_ERROR_INTERNAL
                .getCode());
    }
}