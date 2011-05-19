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

package org.restlet.ext.oauth;

/**
 * The user store interface represents the entry point where user requests are
 * created, searched and removed. It is an excellent indicator where to modify
 * when implementing another persistence model.
 * 
 * @author Kristoffer Gronowski
 */
public abstract class UserStore {

    /**
     * Create a user
     */
    public abstract AuthenticatedUser createUser(String id);

    /**
     * Retrieve a given user
     */
    public abstract AuthenticatedUser findUser(String id);

    /**
     * @return true if this OAuth server contains a given user
     */
    public abstract boolean containsUser(String id);

    /**
     * @param id
     *            of the user to revoke/delete
     */
    public abstract void revokeUser(String id); // Same as delete a user.

}
