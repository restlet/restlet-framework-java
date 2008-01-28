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

import javax.ws.rs.core.SecurityContext;

/**
 * 
 * @author Stephan Koops
 * @see SecurityContext
 */
public interface Authorizator { // FIXME Authorizator.checkSecret ist semantisch falsch

    /**
     * Checks, if the combination of the given identifier and it's secrets
     * is valid.
     * @param identifier
     * @param secret
     * @return true, if the credentials are valid, or false if not.
     * @see org.restlet.Guard#checkSecret(org.restlet.data.Request, String, char[])
     */
    public boolean checkSecret(String identifier, char[] secret);

    /**
     * Checks, if the user is in the given role, or false if not.
     * @param principal The pricipal to check.
     * @param role the role.
     * @return true, if the user is in the role, false otherwise.
     */
    public boolean isUserInRole(Principal principal, String role);
}
