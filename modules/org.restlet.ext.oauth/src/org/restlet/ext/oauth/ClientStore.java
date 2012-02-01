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

import java.util.Collection;

import org.restlet.ext.oauth.internal.TokenGenerator;

// import org.restlet.Context;

/**
 * Abstract class that defines a client store for the Authentication Server. The
 * following code adds a client to a store when you create your inbound root
 * 
 * <pre>
 * {
 * &#064;code
 * public synchronized Restlet createInboundRoot(){
 *   ClientStore clientStore = ClientStoreFactory.getInstance();
 *   clientStore.createClient(&quot;1234567890&quot;,&quot;1234567890&quot;,
 *    &quot;http://localhost:8080&quot;);
 *  
 *   attribs.put(ClientStore.class.getCanonicalName(), clientStore);
 * }
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 */
public abstract class ClientStore<G extends TokenGenerator> {

    // private final Context context;
    /** The token generator. */
    private final G generator;

    /**
     * Constructor.
     * 
     * @param generator
     *            The token generator.
     */
    protected ClientStore(G generator) {
        this.generator = generator;
        // this(generator, Context.getCurrent());
    }

    //
    // protected ClientStore(G generator, Context context) {
    // //this.context = context;
    // this.generator = generator;
    // }

    /**
     * Useful only for clients using the user agent oauth flow where secret is
     * never used
     * 
     * @param clientId
     *            oauth client_id entry for a new client
     * @param redirectUri
     *            the URL that should be used for oauth callbacks
     * @return client POJO
     */
    public abstract Client createClient(String clientId, String redirectUri);

    /**
     * Used for creating a data entry representation for a oauth client
     * 
     * @param clientId
     *            oauth client_id entry for a new client
     * @param clientSecret
     *            oauth client_secret entry for a new client
     * @param redirectUri
     *            the URL that should be used for oauth callbacks
     * @return client POJO
     */
    public abstract Client createClient(String clientId, String clientSecret,
            String redirectUri);

    /**
     * Delete a client_id from the implementing backed database.
     * 
     * @param id
     *            client_id of the client to remove
     */

    public abstract void deleteClient(String id);

    /**
     * Search for a client_id if present in the database.
     * 
     * @param id
     *            client_id to search for.
     * @return client POJO or null if not found.
     */

    public abstract Client findById(String id);

    /**
     * Function to find all granted client for a specific authenticated use.
     * Useful for implementing a revocation page where a specific user should be
     * able to opt out.
     * 
     * @param userid
     *            id of the user to retrieve. (openid)
     * @return active clients or empty collection.
     */
    public abstract Collection<? extends Client> findClientsForUser(
            String userid);

    // public Context getContext() {
    // return context;
    // }

    /**
     * Returns the token generator.
     * 
     * @return an instance of the TokenGenerator
     */
    public G getTokenGenerator() {
        return generator;
    }

}
