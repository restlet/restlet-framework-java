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

package org.restlet.ext.oauth.internal;

import java.util.Map;

import org.restlet.ext.oauth.internal.Client.ClientType;

/**
 * Abstract class that defines a client store for the Authorization Server. The
 * following code adds a client to a store when you create your inbound root
 * 
 * <pre>
 * {
 * &#064;code
 * public synchronized Restlet createInboundRoot(){
 *   ClientManager clientStore = ClientStoreFactory.getInstance();
 *   clientStore.createClient(&quot;1234567890&quot;,&quot;1234567890&quot;,
 *    &quot;http://localhost:8080&quot;);
 *  
 *   attribs.put(ClientManager.class.getCanonicalName(), clientStore);
 * }
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public interface ClientManager {

    /**
     * Used for creating a data entry representation for a oauth client
     * 
     * @param clientType
     * @param redirectURIs
     * @param properties
     * @return
     */
    Client createClient(ClientType clientType, String[] redirectURIs,
            Map<String, Object> properties);

    /**
     * Delete a client_id from the implementing backed database.
     * 
     * @param id
     *            client_id of the client to remove
     */

    void deleteClient(String id);

    /**
     * Search for a client_id if present in the database.
     * 
     * @param id
     *            client_id to search for.
     * @return client POJO or null if not found.
     */

    Client findById(String id);

}
