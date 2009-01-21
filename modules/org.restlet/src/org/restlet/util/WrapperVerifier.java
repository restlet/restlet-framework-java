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

package org.restlet.util;

import org.restlet.security.Verifier;

/**
 * Verifier that decorates another verifier.
 * 
 * @author Jerome Louvel
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 */
public class WrapperVerifier extends Verifier {

    /** The wrapped verifier. */
    private volatile Verifier wrappedVerifier;

    /**
     * Constructor.
     * 
     * @param wrappedVerifier
     *            The wrapped verifier.
     */
    public WrapperVerifier(Verifier wrappedVerifier) {
        this.wrappedVerifier = wrappedVerifier;
    }

    /**
     * Returns the wrapped verifier.
     * 
     * @return The wrapped verifier.
     */
    public Verifier getWrappedVerifier() {
        return wrappedVerifier;
    }

    /**
     * Sets the wrapped verifier.
     * 
     * @param wrappedVerifier
     *            The wrapped verifier.
     */
    public void setWrappedVerifier(Verifier wrappedVerifier) {
        this.wrappedVerifier = wrappedVerifier;
    }

    /**
     * Delegates the verification to the wrapped verifier.
     * 
     * @param identifier
     *            The user identifier.
     * @param secret
     *            The proposed secret.
     * @return True if the proposed secret is correct.
     */
    @Override
    public boolean verify(String identifier, char[] secret) {
        return getWrappedVerifier().verify(identifier, secret);
    }

}
