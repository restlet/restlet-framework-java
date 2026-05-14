/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jaas;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.restlet.Request;
import org.restlet.Response;

/**
 * JAAS callback handler that automatically provides the identifier and secret when asked by login
 * modules.
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
     * @param request The handled request.
     * @param response The handled response.
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
     * Handles a callback. The default implementation automatically sets the identifier on {@link
     * NameCallback} instances and the secret on {@link PasswordCallback}.
     *
     * @param callback The callback to handle.
     * @throws UnsupportedCallbackException
     */
    protected void handle(Callback callback) throws UnsupportedCallbackException {
        switch (callback) {
            case NameCallback nameCallback when getRequest().getChallengeResponse() != null ->
                    nameCallback.setName(getRequest().getChallengeResponse().getIdentifier());
            case PasswordCallback passwordCallback
                    when getRequest().getChallengeResponse() != null ->
                    passwordCallback.setPassword(getRequest().getChallengeResponse().getSecret());
            default -> throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
        }
    }

    /**
     * Handles the callbacks. The default implementation delegates the handling to the {@link
     * #handle(Callback)} method.
     *
     * @param callbacks The callbacks to handle.
     */
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

        if (callbacks != null) {
            for (Callback callback : callbacks) {
                handle(callback);
            }
        }
    }

    /**
     * Sets the handled request.
     *
     * @param request The handled request.
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the handled response.
     *
     * @param response The handled response.
     */
    public void setResponse(Response response) {
        this.response = response;
    }
}
