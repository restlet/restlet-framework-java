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

package org.restlet.ext.oauth.internal.memory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.ext.oauth.internal.AbstractClientManager;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;

/**
 * Memory implementation of ClientManager interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MemoryClientManager extends AbstractClientManager {

    private final Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    protected Client createClient(String clientId, char[] clientSecret,
            ClientType clientType, String[] redirectURIs,
            Map<String, Object> properties) {
        MemoryClient client = new MemoryClient(UUID.randomUUID().toString(),
                clientType, redirectURIs, properties);
        if (clientSecret != null) {
            client.setClientSecret(clientSecret);
        }

        clients.put(client.getClientId(), client);

        return client;
    }

    public void deleteClient(String id) {
        clients.remove(id);
    }

    public Client findById(String id) {
        return clients.get(id);
    }
}
