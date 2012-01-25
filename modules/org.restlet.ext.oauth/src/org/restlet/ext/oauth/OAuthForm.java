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

package org.restlet.ext.oauth;

import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;

/**
 * Helper class used when the OAuth token should be transmitted as a form. Only
 * viable for HTTP PUT, POST and DELETE Still the best practice as a client is
 * to set the token in "Authorization" HTTP header.
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthForm extends Form {
    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     */
    public OAuthForm(String token) {
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param initialCapacity
     *            The initial list capacity.
     */
    public OAuthForm(String token, int initialCapacity) {
        super(initialCapacity);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param delegate
     *            The delegate list.
     */
    public OAuthForm(String token, List<Parameter> delegate) {
        super(delegate);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param webForm
     *            The URL encoded Web form.
     */
    public OAuthForm(String token, Representation webForm) {
        super(webForm);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param queryString
     *            The Web form parameters as a string.
     */
    public OAuthForm(String token, String queryString) {
        super(queryString);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param parametersString
     *            The parameters string to parse.
     * @param separator
     *            The separator character to append between parameters.
     */
    public OAuthForm(String token, String parametersString, char separator) {
        super(parametersString, separator);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param queryString
     *            The Web form parameters as a string.
     * @param characterSet
     *            The supported character encoding.
     */
    public OAuthForm(String token, String queryString, CharacterSet characterSet) {
        super(queryString, characterSet);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Constructor.
     * 
     * @param token
     *            The value of the {@link OAuthServerResource#OAUTH_TOKEN}
     *            token.
     * @param parametersString
     *            The parameters string to parse.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between parameters.
     */
    public OAuthForm(String token, String parametersString,
            CharacterSet characterSet, char separator) {
        super(parametersString, characterSet, separator);
        add(OAuthServerResource.OAUTH_TOKEN, token);
    }

    /**
     * Returns the value of the {@link OAuthServerResource#OAUTH_TOKEN} token.
     * 
     * @return The value of the {@link OAuthServerResource#OAUTH_TOKEN} token.
     */
    public String getOAuthToken() {
        return this.getFirstValue(OAuthServerResource.OAUTH_TOKEN);
    }

}
