/**
 * Copyright 2005-2014 Restlet S.A.S.
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuthParameters implements OAuthResourceDefs {

    private Form form;

    public OAuthParameters() {
        form = new Form();
    }

    @Override
    public String toString() {
        return form.getQueryString();
    }

    // protected OAuthParameters clientId(String clientId) {
    // form.add(CLIENT_ID, clientId);
    // return this;
    // }
    //
    // protected OAuthParameters clientSecret(String clientSecret) {
    // form.add(CLIENT_SECRET, clientSecret);
    // return this;
    // }

    public OAuthParameters responseType(ResponseType responseType) {
        add(RESPONSE_TYPE, responseType.name());
        return this;
    }

    public OAuthParameters grantType(GrantType grantType) {
        add(GRANT_TYPE, grantType.name());
        return this;
    }

    public OAuthParameters code(String code) {
        add(CODE, code);
        return this;
    }

    public OAuthParameters redirectURI(String redirectURI) {
        add(REDIR_URI, redirectURI);
        return this;
    }

    public OAuthParameters username(String username) {
        add(USERNAME, username);
        return this;
    }

    public OAuthParameters password(String password) {
        add(PASSWORD, password);
        return this;
    }

    public OAuthParameters refreshToken(String refreshToken) {
        add(REFRESH_TOKEN, refreshToken);
        return this;
    }

    public OAuthParameters scope(String[] scope) {
        add(SCOPE, Scopes.toString(scope));
        return this;
    }

    public OAuthParameters state(String state) {
        add(STATE, state);
        return this;
    }

    public OAuthParameters add(String name, String value) {
        form.add(name, value);
        return this;
    }

    public Representation toRepresentation() {
        return form.getWebRepresentation();
    }

    public Reference toReference(String uri) {
        String query;
        try {
            query = form.encode();
        } catch (IOException ex) {
            Logger.getLogger(OAuthParameters.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new ResourceException(ex);
        }
        Reference reference = new Reference(uri);
        reference.setQuery(query);
        return reference;
    }

    protected Form toForm() {
        return form;
    }
}
