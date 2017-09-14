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

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * Gathers OAuth credentials.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuthParameters implements OAuthResourceDefs {
    /** The list of parameters. */
    private Form form;

    /**
     * Constructor.
     */
    public OAuthParameters() {
        form = new Form();
    }

    /**
     * Add a new parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param value
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters add(String name, String value) {
        form.add(name, value);
        return this;
    }

    /**
     * Adds a {@link OAuthResourceDefs#CODE} parameter.
     * 
     * @param code
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters code(String code) {
        return add(CODE, code);
    }

    /**
     * Adds a {@link OAuthResourceDefs#GRANT_TYPE} parameter.
     * 
     * @param grantType
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters grantType(GrantType grantType) {
        return add(GRANT_TYPE, grantType.name());
    }

    /**
     * Adds a {@link OAuthResourceDefs#PASSWORD} parameter.
     * 
     * @param password
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters password(String password) {
        return add(PASSWORD, password);
    }

    /**
     * Adds a {@link OAuthResourceDefs#REDIR_URI} parameter.
     * 
     * @param redirectURI
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters redirectURI(String redirectURI) {
        return add(REDIR_URI, redirectURI);
    }

    /**
     * Adds a {@link OAuthResourceDefs#REFRESH_TOKEN} parameter.
     * 
     * @param refreshToken
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters refreshToken(String refreshToken) {
        return add(REFRESH_TOKEN, refreshToken);
    }

    /**
     * Adds a {@link OAuthResourceDefs#RESPONSE_TYPE} parameter.
     * 
     * @param responseType
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters responseType(ResponseType responseType) {
        return add(RESPONSE_TYPE, responseType.name());
    }

    /**
     * Adds a {@link OAuthResourceDefs#SCOPE} parameter.
     * 
     * @param scope
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters scope(String[] scope) {
        return add(SCOPE, Scopes.toString(scope));
    }

    /**
     * Adds a {@link OAuthResourceDefs#STATE} parameter.
     * 
     * @param state
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters state(String state) {
        return add(STATE, state);
    }

    protected Form toForm() {
        return form;
    }

    /**
     * Completes the URI with the OAuth parameters as query parameters.
     * 
     * @param uri
     *            The URI to complete.
     * @return The URI with the set as Oauth parameters as query.
     */
    public Reference toReference(String uri) {
        try {
            Reference reference = new Reference(uri);
            reference.setQuery(form.encode());

            return reference;
        } catch (IOException ex) {
            Logger.getLogger(OAuthParameters.class.getName()).log(Level.SEVERE,
                    "Issue when encoding the OAuth parameters.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Issue when encoding the OAuth parameters.", ex);
        }
    }

    /**
     * Completes the URI with the OAuth parameters as query parameters.
     * 
     * @param ref
     *            The URI to complete.
     * @return The URI with the set as Oauth parameters as query.
     */
    public Reference toReference(Reference ref) {
        Reference reference = new Reference(ref);

        // Add each parameter to avoid overwriting existing parameters
        for (Parameter param : form) {
            reference.addQueryParameter(param);
        }

        return reference;
    }

    /**
     * Returns the set of Oauth parameters as a web form.
     * 
     * @return The set of Oauth parameters as a web form.
     */
    public Representation toRepresentation() {
        return form.getWebRepresentation();
    }

    @Override
    public String toString() {
        return form.getQueryString();
    }

    /**
     * Adds a {@link OAuthResourceDefs#USERNAME} parameter.
     * 
     * @param username
     *            The value of the parameter.
     * @return The current instance, in order to chain calls.
     */
    public OAuthParameters username(String username) {
        return add(USERNAME, username);
    }
}
