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

import org.restlet.Guard;

/**
 * <p>
 * Because Restlet does not support its own user and role management (as e.g.
 * the Servlet API), you must implement it, if you need it for JAX-RS.<br>
 * This interface is used to check, if a user is in a role. Implementations must
 * be thread save.
 * </p>
 * <p>
 * This interface is used by {@link SecurityContext#isUserInRole(String)}. The
 * JAX-RS runtime delegates this method call along with the {@link Principal} of
 * the HTTP request to method {@link #isUserInRole(Principal, String)}, the
 * only method of this interface.
 * </p>
 * <p>
 * If you need user access control, you must give the {@link JaxRsRouter} or the
 * {@link JaxRsApplication} an instance of this interface, see
 * {@link JaxRsRouter#JaxRsRouter(org.restlet.Context, javax.ws.rs.core.ApplicationConfig, AccessControl)},
 * {@link JaxRsRouter#JaxRsRouter(org.restlet.Context, AccessControl)},
 * {@link JaxRsRouter#setAccessControl(AccessControl)} or
 * {@link JaxRsApplication#setAccessControl(AccessControl)} If you not give an
 * instance, every call of {@link SecurityContext#isUserInRole(String)} results
 * in an Internal Server Error, which will get returned to the client (see
 * {@link ThrowExcAccessControl}).
 * </p>
 * <p>
 * To check if the user is authenticated, use any Restlet {@link Guard}.
 * </p>
 * 
 * @author Stephan Koops
 * @see ForbidAllAccess If using this access control, no user has any role.
 * @see AllowAllAccess If using this access control, every user has every role.
 * @see ThrowExcAccessControl If using this access control, every call of
 *      {@link SecurityContext#isUserInRole(String)} results in an Internal
 *      Server Error, which will get returned to the client.
 * @see SecurityContext
 */
public interface AccessControl {

    /**
     * Checks, if the user is in the given role, or false if not.<br>
     * This method is used by the {@link SecurityContext}.
     * 
     * @param principal
     *                The principal to check.
     * @param role
     *                the role.
     * @return true, if the user is in the role, false otherwise.
     * @throws WebApplicationException
     *                 The developer may handle exceptions by throw a
     *                 {@link WebApplicationException}.
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isUserInRole(Principal principal, String role)
            throws WebApplicationException;
}