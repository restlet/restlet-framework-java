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

package org.restlet.ext.oauth.provider.data.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.ext.oauth.provider.data.Client;
import org.restlet.ext.oauth.provider.data.ClientStore;

/**
 * In memory client store is keeping client_id, client_secret and redirict URL
 * Note that clients will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class MemClientStore extends ClientStore<MemTokenGenerator> {

    private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    public MemClientStore() {
        super(new MemTokenGenerator(new ScheduledThreadPoolExecutor(5)));
    }

    public MemClientStore(ScheduledThreadPoolExecutor executor) {
        super(new MemTokenGenerator(executor));
    }

    public Client createClient(String clientId, String redirectUri) {
        return createClient(clientId, null, redirectUri);
    }

    public Client createClient(String clientId, String clientSecret,
            String redirectUri) {
        Client client = new ClientImpl(clientId, clientSecret, redirectUri);
        clients.put(clientId, client);
        return client;
    }

    public void deleteClient(String id) {
        clients.remove(id);
    }

    public Client findById(String id) {
        return clients.get(id);
    }

    @Override
    public Collection<Client> findClientsForUser(String userid) {
        ArrayList<Client> result = new ArrayList<Client>();
        for (Client c : clients.values()) {
            if (c.containsUser(userid))
                result.add(c);
        }
        return result;
    }
}
