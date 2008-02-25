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
import javax.ws.rs.core.SecurityContext;

/**
 * Interface to check user access.
 * 
 * @author Stephan Koops
 * @see ForbidAllAuthenticator
 * @see AllowAllAuthenticator
 * @see ThrowExcAuthenticator
 * @see SecurityContext
 */
public interface Authenticator {

    /**
     * <p>
     * Checks, if the user is in the given role, or false if not.
     * </p>
     * <p>
     * This method is used by the {@link SecurityContext}.
     * </p>
     * 
     * @param principal
     *                The principal to check.
     * @param role
     *                the role.
     * @return true, if the user is in the role, false otherwise.
     * @throws WebApplicationException
     *                 The developer may handle exceptions by throw a
     *                 {@link WebApplicationException}. If this method must not
     *                 be used in a concrete implementation, it could also throw
     *                 an {@link WebApplicationException}, e.g. Status 500
     *                 (Internal Server Error)
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isUserInRole(Principal principal, String role)
            throws WebApplicationException;
}
