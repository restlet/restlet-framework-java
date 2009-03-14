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

package org.restlet.engine.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * JAAS callback handler that automatically provides the identifier and secret
 * when asked by login modules.
 * 
 * @author Jerome Louvel
 */
public class DefaultJaasCallbackHandler implements CallbackHandler {

    /** The handled request. */
    private volatile Request request;

    /** The handled response. */
    private volatile Response response;

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public DefaultJaasCallbackHandler(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Returns the handled request.
     * 
     * @return The handled request.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the handled response.
     * 
     * @return The handled response.
     */
    public Response getResponse() {
        return response;
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
            nc.setName(getRequest().getChallengeResponse().getIdentifier());
        } else if (callback instanceof PasswordCallback) {
            PasswordCallback pc = (PasswordCallback) callback;
            pc.setPassword(getRequest().getChallengeResponse().getSecret());
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
     * Sets the handled request.
     * 
     * @param request
     *            The handled request.
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the handled response.
     * 
     * @param response
     *            The handled response.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

}
