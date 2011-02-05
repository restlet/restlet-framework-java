/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.security;

import java.security.Principal;

import org.restlet.data.ClientInfo;

/**
 * Updates an authenticated client user with assigned roles. Typically, it is
 * invoked by an {@link Authenticator} after successful authentication to add
 * {@link Role} instances based on available {@link User}.
 * 
 * @see Authenticator#getEnroler()
 * @see Authenticator#setEnroler(Enroler)
 * @see ClientInfo#getUser()
 * @see ClientInfo#getRoles()
 * @author Jerome Louvel
 */
public interface Enroler {

    /**
     * Attempts to update an authenticated client, with a {@link User} properly
     * defined, by adding the {@link Role} that are assigned to this user. Note
     * that principals could also be added to the {@link ClientInfo} if
     * necessary. The addition could also potentially be based on the presence
     * of {@link Principal}.
     * 
     * @param clientInfo
     *            The clientInfo to update.
     */
    public void enrole(ClientInfo clientInfo);

}
