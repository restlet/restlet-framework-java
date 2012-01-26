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

package org.restlet.example.ext.oauth.experimental;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.ClientStore;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;

/**
 * OAuth web flow proxy class used for setting up a callback channel in the
 * reverse direction so that two resources can establish a bi-directional trust
 * and secure communication.
 * 
 * The back channel is supposed to be using the OAuth 2 grant_type none follow
 * for establishing a token in the reverse direction. For this purpose the
 * entity running the proxy must also have a fully functioning Authorization
 * server where protected resources would implement the callback functions. What
 * the callback function does is beyond the scope of the OAuth lib.
 * 
 * It only makes sure that there is a restricted OAuth token in the reverse
 * direction. How the client_id and client_secret is propagated is ooutside of
 * the scope for this library.
 * 
 * Example web client side code:
 * 
 * Creates a local auth server endpoint for incoming none flow
 * 
 * <pre>
 * {
 *     &#064;code
 *     Finder at = new Finder(loginCtx, AccessTokenResource.class);
 *     router.attach(&quot;/access_token&quot;, at);
 * 
 *     OauthParameters cb = new OauthParameters(&quot;1234&quot;, &quot;1234&quot;, &quot;N/A&quot;);
 *     CallbackProxy cbp = new CallbackProxy(params, cb, loginCtx);
 *     Finder f = new Finder(loginCtx, ExchangeTokenResource.class);
 *     cbp.setNext(f);
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10">OAuth 2
 *      Draft</a>
 */
public class CallbackProxy extends OAuthProxy {

    protected ClientStore<?> clients;

    /**
     * Gets the ClientStore from the application context. If the web server flow
     * component is deployed in a different class loader make sure to use the
     * factory of client store to initialize and not the constructor.
     * 
     * @param params
     *            OAuth parameters for the client session
     * @param ctx
     *            Restlet context for the application
     */
    public CallbackProxy(OAuthParameters params, Context ctx) {
        super(params, ctx);

        ConcurrentMap<String, Object> attribs = ctx.getAttributes();

        clients = (ClientStore<?>) attribs.get(ClientStore.class
                .getCanonicalName());
        getLogger().info("Found client store = " + clients);
    }

    /**
     * Constructor that sets up credentials in both the normal web_server/code
     * direction as a normal OauthProxy would do and also establishes an
     * incoming identity for an autonomous client back.
     * 
     * Combination of calling: CallbackProxy cp = new CallbackProxy(params,ctx);
     * cp.setCallback(autonomous)
     * 
     * @param params
     *            standard oauth client parameters for the code token flow
     * @param callback
     *            values used when acting autonomous auth server
     * @param ctx
     *            Restlet context for the application
     */

    public CallbackProxy(OAuthParameters params, OAuthParameters callback,
            Context ctx) {
        this(params, ctx);
        setCallback(callback);
    }

    /**
     * Sets up the reverse direction auth credentials. The provided parameters
     * are stored in a local auth repository. When the standard code flow is
     * completed the protected resource can then contact the CallbackProxy back
     * using the none autonomous flow.
     * 
     * How the client_id and client_secret are transferred in a secure way is
     * outside of the scope of this library.
     * 
     * One example is that the protected resource is receiving the id and secret
     * in a protected resource and then initiates a client side none flow.
     * 
     * The result is that both side ends up with a access token. This can then
     * be used to establish a secure bi-directional trust and secure
     * communication.
     * 
     * @param callback
     *            values used when acting autonomous auth server
     */
    public void setCallback(OAuthParameters callback) {
        String clientId = callback.getClientId();
        String clientSecret = callback.getClientSecret();
        // TODO maybe have to add callbackUrl if other back scenario then none
        // flow

        Client client = clients.findById(clientId);

        if (client == null)
            client = clients.createClient(clientId, clientSecret, /* redirUri */
            null);

        getLogger().info(
                "Registered callback client - " + clientId + " : "
                        + clientSecret);
    }

}
