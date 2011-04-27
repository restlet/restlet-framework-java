/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.oauth;

import java.util.Collection;

// import org.restlet.Context;

/**
 * Abstract class that defines a client store for the Authentication Server. The
 * following code adds a client to a store when you create your inbound root
 * 
 * <pre>
 * {@code
 * public synchronized Restlet createInboundRoot(){
 *   ClientStore clientStore = ClientStoreFactory.getInstance();
 *   clientStore.createClient("1234567890","1234567890", "http://localhost:8080");
 *  
 *   attribs.put(ClientStore.class.getCanonicalName(), clientStore);
 * }
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 */
public abstract class ClientStore <G extends TokenGenerator> {

    //private final Context context;

    private final G generator;

    protected ClientStore(G generator) {
    	this.generator = generator;
        //this(generator, Context.getCurrent());
    }
//
//    protected ClientStore(G generator, Context context) {
//        //this.context = context;
//        this.generator = generator;
//    }

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
    public abstract Collection<? extends Client> findClientsForUser(String userid);

//    public Context getContext() {
//        return context;
//    }

    /**
     * @return an instance of the TokenGenerator
     */

    public G getTokenGenerator() {
        return generator;
    }

}
