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

package org.restlet.ext.oauth.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.ClientStore;

/**
 * In memory client store is keeping client_id, client_secret and redirict URL
 * Note that clients will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class MemClientStore extends ClientStore<MemTokenGenerator> {

    private final Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

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
