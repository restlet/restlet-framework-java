/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.security;

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Security realm capable of providing an enroler and a verifier.
 * 
 * @author Jerome Louvel
 */
public abstract class Realm {

    /** The name. */
    private volatile String name;

    /** The modifiable series of parameters. */
    private final Series<Parameter> parameters;

    /**
     * The enroler that can add the user roles based on user principals.
     */
    private volatile Enroler enroler;

    /**
     * The verifier that can check the validity of the user credentials
     * associated to a request.
     */
    private volatile Verifier verifier;

    /** Indicates if the realm was started. */
    private volatile boolean started;

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
        this.parameters = new Series<Parameter>(Parameter.class,
                new CopyOnWriteArrayList<Parameter>());
        this.started = false;
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
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
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
     * Indicates if the realm is started.
     * 
     * @return True if the realm is started.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Indicates if the realm is stopped.
     * 
     * @return True if the realm is stopped.
     */
    public boolean isStopped() {
        return !this.started;
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
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the modifiable series of parameters. This method clears the current
     * series and adds all entries in the parameter series.
     * 
     * @param parameters
     *            A series of parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        synchronized (getParameters()) {
            if (parameters != getParameters()) {
                getParameters().clear();

                if (parameters != null) {
                    getParameters().addAll(parameters);
                }
            }
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

    /** Starts the realm. */
    public synchronized void start() throws Exception {
        this.started = true;
    }

    /** Stops the realm. */
    public synchronized void stop() throws Exception {
        this.started = false;
    }

    @Override
    public String toString() {
        return getName();
    }

}
