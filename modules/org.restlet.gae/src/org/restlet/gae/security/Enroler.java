/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gae.security;

import java.security.Principal;

import javax.security.auth.Subject;

/**
 * Updates an authenticated subject with new principals. Typically, it is
 * invoked by an {@link Authenticator} after successful authentication to add
 * {@link RolePrincipal} instances based on available {@link UserPrincipal}.
 * 
 * @author Jerome Louvel
 * @see Authenticator#getEnroler()
 * @see Authenticator#setEnroler(Enroler)
 */
public abstract class Enroler {

    /**
     * Attempts to update an authenticated subject, with a {@link UserPrincipal}
     * properly defined, by adding the {@link RolePrincipal} that are assigned
     * to this user.<br>
     * <br>
     * Note that other types of principals could also be added to the subject if
     * necessary. The addition could also potentially be based on the presence
     * of other type of {@link Principal}.
     * 
     * @param subject
     *            The subject to update.
     */
    public abstract void enrole(Subject subject);

}
