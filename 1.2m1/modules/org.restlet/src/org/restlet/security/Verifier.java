/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import javax.security.auth.Subject;

/**
 * Generic verifier of secrets. Frequently, the secret will correspond to a
 * passwords.
 * 
 * @author Jerome Louvel
 */
public interface Verifier {

    /**
     * Verifies that the proposed secret is correct for the specified
     * identifier. Frequently, the identifier will correspond to a user login
     * and the secret to a password, but this isn't always the case.
     * 
     * @param subject
     *            The subject to update with principals.
     * @param identifier
     *            The user identifier.
     * @param secret
     *            The proposed secret.
     * @return True if the proposed secret was correct and the subject updated.
     */
    public boolean verify(Subject subject, String identifier, char[] secret);

}
