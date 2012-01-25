/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.restlet.Request;
import org.restlet.Response;

/**
 * JAAS callback handler that automatically provides the identifier and secret
 * when asked by login modules.
 * 
 * @author Jerome Louvel
 */
public class ChallengeCallbackHandler implements CallbackHandler {

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
    public ChallengeCallbackHandler(Request request, Response response) {
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
     * identifier on {@link javax.security.auth.callback.NameCallback} instances
     * and the secret on {@link PasswordCallback}.
     * 
     * @param callback
     *            The callback to handle.
     * @throws UnsupportedCallbackException
     */
    protected void handle(Callback callback)
            throws UnsupportedCallbackException {
        // [ifndef android]
        if (callback instanceof javax.security.auth.callback.NameCallback) {
            javax.security.auth.callback.NameCallback nc = (javax.security.auth.callback.NameCallback) callback;

            if (getRequest().getChallengeResponse() != null) {
                nc.setName(getRequest().getChallengeResponse().getIdentifier());
            }
        } else
        // [enddef]
        if (callback instanceof PasswordCallback) {
            PasswordCallback pc = (PasswordCallback) callback;

            if (getRequest().getChallengeResponse() != null) {
                pc.setPassword(getRequest().getChallengeResponse().getSecret());
            }
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
