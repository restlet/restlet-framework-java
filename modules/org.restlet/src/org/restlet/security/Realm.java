/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Security realm capable of providing an enroler and a verifier.
 * 
 * @author Jerome Louvel
 */
public abstract class Realm {

    /** The modifiable series of parameters. */
    private final Series<Parameter> parameters;

    /**
     * The enroler that can add the user roles based on user principals.
     */
    private volatile Enroler enroler;

    /**
     * The verifier that can check the validity of the credentials associated to
     * a request.
     */
    private volatile Verifier verifier;

    /**
     * Constructor.
     */
    public Realm() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param verifier
     *            The verifier that can check the validity of the credentials
     *            associated to a request.
     * 
     * @param enroler
     *            The enroler that can add the user roles based on user
     *            principals.
     */
    public Realm(Verifier verifier, Enroler enroler) {
        this.enroler = enroler;
        this.verifier = verifier;
        this.parameters = new Form(new CopyOnWriteArrayList<Parameter>());
    }

    /**
     * Returns an enroler that can add the user roles based on user principals.
     * 
     * @return An enroler.
     */
    public Enroler getEnroler() {
        return enroler;
    }

    /**
     * Returns the modifiable series of parameters. A parameter is a pair
     * composed of a name and a value and is typically used for configuration
     * purpose, like Java properties. Note that multiple parameters with the
     * same name can be declared and accessed.
     * 
     * @return The modifiable series of parameters.
     */
    public Series<Parameter> getParameters() {
        return this.parameters;
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
     * Sets an enroler that can add the user roles based on user principals.
     * 
     * @param enroler
     *            An enroler.
     */
    public void setEnroler(Enroler enroler) {
        this.enroler = enroler;
    }

    /**
     * Sets the modifiable series of parameters.
     * 
     * @param parameters
     *            The modifiable series of parameters.
     */
    public synchronized void setParameters(Series<Parameter> parameters) {
        this.parameters.clear();

        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    /**
     * Sets a verifier that can check the validity of the credentials associated
     * to a request.
     * 
     * @param verifier
     *            A local verifier.
     */
    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }

}
