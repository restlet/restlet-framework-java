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

package org.restlet.ext.jaxrs.core;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author Stephan Koops
 *
 */
public class PrincipalImpl implements Principal, Serializable {

    private static final long serialVersionUID = -1842197948591956691L;
    
    private String name;
    
    @SuppressWarnings("unused")
    private PrincipalImpl() {
        // constructor for deserialization
    }
    
    /**
     * Creates a new Principal with the given name
     * @param name The name of the Principal; must not be null
     */
    public PrincipalImpl(String name)
    {
        if(name == null)
            throw new IllegalArgumentException("The name must not be null");
        this.name = name;
    }

    /**
     * Compares this principal to the specified object. Returns true if the
     * object passed in matches the principal represented by the implementation
     * of this interface.
     * 
     * @param another
     *                principal to compare with.
     * 
     * @return true if the principal passed in is the same as that encapsulated
     *         by this principal, and false otherwise.
     * @see
     */
    @Override
    public boolean equals(Object another) {
        if (another == this)
            return true;
        if (!(another instanceof Principal))
            return false;
        Principal otherPrinc = (Principal) another;
        return getName().equals(otherPrinc.getName());
    }

    /**
     * Returns the name of this principal.
     * 
     * @return the name of this principal.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a hashcode for this principal.
     * 
     * @return a hashcode for this principal.
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Returns a string representation of this principal.
     * 
     * @return a string representation of this principal.
     */
    @Override
    public String toString() {
        return getName();
    }
}