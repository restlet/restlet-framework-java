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

/**
 * Security realm capable of providing an enroler and a verifier.
 * 
 * @author Jerome Louvel
 */
public abstract class Realm {

    /**
     * The enroler that can add the user roles based on Restlet default
     * authorization model.
     */
    private volatile Enroler enroler;

    /**
     * The verifier that can check the validity of user/secret couples based on
     * Restlet default authorization model.
     */
    private volatile Verifier verifier;

    public Realm() {
        this(null, null);
    }

    public Realm(Verifier verifier, Enroler enroler) {
        this.enroler = enroler;
        this.verifier = verifier;
    }

    /**
     * Returns a local enroler that can add the user roles based on Restlet
     * default authorization model.
     * 
     * @return An enroler.
     */
    public Enroler getEnroler() {
        return enroler;
    }

    /**
     * Returns a verifier that can check the validity of the credentials
     * associated to a request.
     * 
     * @return A verifier.
     */
    public Verifier getVerifier() {
        return this.verifier;
    }

    /**
     * Sets a local enroler that can add the user roles based on Restlet default
     * authorization model.
     * 
     * @param enroler
     *            An enroler.
     */
    public void setEnroler(Enroler enroler) {
        this.enroler = enroler;
    }

    /**
     * Sets a local verifier that can check the validity of user/secret couples
     * based on Restlet default authorization model.
     * 
     * @param verifier
     *            A local verifier.
     */
    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }

}
