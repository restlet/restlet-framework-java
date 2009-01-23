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

package org.restlet.engine.authentication;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * JAAS callback handler that automatically provides the identifier and secret
 * when asked by login modules.
 * 
 * @author Jerome Louvel
 */
public class JaasCallbackHandler implements CallbackHandler {

    /** The identifier such as user login. */
    private volatile String identifier;

    /** The secret such as password. */
    private char[] secret;

    /** The subject containing identity information. */
    private volatile Subject subject;

    /**
     * Constructor.
     * 
     * @param subject
     *            The subject containing identity information.
     * @param identifier
     *            The identifier such as user login.
     * @param secret
     *            The secret such as password.
     */
    public JaasCallbackHandler(Subject subject, String identifier, char[] secret) {
        this.setSubject(subject);
        this.setIdentifier(identifier);
        this.setSecret(secret);
    }

    /**
     * Returns the identifier such as user login.
     * 
     * @return The identifier such as user login.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the secret such as password.
     * 
     * @return The secret such as password.
     */
    public char[] getSecret() {
        return secret;
    }

    /**
     * Returns the subject containing identity information.
     * 
     * @return The subject containing identity information.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Handles a callback. The default implementation automatically sets the
     * identifier on {@link NameCallback} instances and the secret on
     * {@link PasswordCallback}.
     * 
     * @param callback
     *            The callback to handle.
     * @throws UnsupportedCallbackException
     */
    protected void handle(Callback callback)
            throws UnsupportedCallbackException {
        if (callback instanceof NameCallback) {
            NameCallback nc = (NameCallback) callback;
            nc.setName(getIdentifier());
        } else if (callback instanceof PasswordCallback) {
            PasswordCallback pc = (PasswordCallback) callback;
            pc.setPassword(getSecret());
        } else {
            throw new UnsupportedCallbackException(callback,
                    "Unrecognized Callback");
        }
    }

    /**
     * Handles the callbacks. The default implementation delegates the handling
     * to the {@link #handle(Callback)} method.
     * 
     * @param callbacks
     *            The callbacks to handle.
     */
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        if (callbacks != null) {
            for (Callback callback : callbacks) {
                handle(callback);
            }
        }
    }

    /**
     * Sets the identifier such as user login.
     * 
     * @param identifier
     *            The identifier such as user login.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the secret such as password.
     * 
     * @param secret
     *            The secret such as password.
     */
    public void setSecret(char[] secret) {
        this.secret = secret;
    }

    /**
     * Sets the subject containing identity information.
     * 
     * @param subject
     *            The subject containing identity information.
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

}
