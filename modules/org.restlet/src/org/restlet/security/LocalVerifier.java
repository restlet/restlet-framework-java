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
 * Verifier that stores its secrets in a map indexed by the identifier.
 * 
 * @author Jerome Louvel
 */
public abstract class LocalVerifier implements Verifier {

    /**
     * Returns the secret associated to a given identifier.
     * 
     * @param identifier
     *            The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    protected abstract char[] getSecret(String identifier);

    /**
     * Verifies that the proposed secret is correct for the specified
     * identifier. By default, it compares the inputSecret with the one obtain
     * by the {@link #getSecret(String)} method and adds a new {@link Principal}
     * instance to the subject if successful.
     * 
     * @param subject
     *            The subject to update with principals.
     * @param identifier
     *            The user identifier.
     * @param inputSecret
     *            The proposed secret.
     * @return True if the proposed secret was correct and the subject updated.
     */
    public boolean verify(Subject subject, String identifier, char[] inputSecret) {
        boolean result = false;
        final char[] outputSecret = getSecret(identifier);

        if ((inputSecret == null) || (outputSecret == null)) {
            // Check if both are null
            result = (inputSecret == outputSecret);
        } else {
            // None is null
            if (inputSecret.length == outputSecret.length) {
                boolean equals = true;

                for (int i = 0; (i < inputSecret.length) && equals; i++) {
                    equals = (inputSecret[i] == outputSecret[i]);
                }

                result = equals;
            }
        }

        if (result) {
            subject.getPrincipals().add(new Principal(identifier));
        }

        return result;
    }

}
