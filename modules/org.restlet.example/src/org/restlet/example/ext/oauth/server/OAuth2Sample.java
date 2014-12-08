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

package org.restlet.example.ext.oauth.server;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.ext.oauth.internal.memory.MemoryClientManager;
import org.restlet.ext.oauth.internal.memory.MemoryTokenManager;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuth2Sample {

    private static SampleUserManager userManager;

    private static ClientManager clientManager;

    private static TokenManager tokenManager;

    protected static SampleUserManager getSampleUserManager() {
        return userManager;
    }

    protected static ClientManager getClientManager() {
        return clientManager;
    }

    protected static TokenManager getTokenManager() {
        return tokenManager;
    }

    public static void main(String[] args) throws Exception {
        userManager = new SampleUserManager();
        userManager.addUser("alice").setPassword("abcdef".toCharArray());
        userManager.addUser("bob").setPassword("123456".toCharArray());

        clientManager = new MemoryClientManager();
        Client client = clientManager.createClient(ClientType.CONFIDENTIAL,
                null, null);
        System.out.println("SampleClient: client_id=" + client.getClientId()
                + ", client_secret="
                + String.copyValueOf(client.getClientSecret()));

        tokenManager = new MemoryTokenManager();

        // Setup Restlet
        Component component = new Component();
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);
        component.getClients().add(Protocol.RIAP);
        component.getClients().add(Protocol.CLAP);
        component.getServers().add(Protocol.HTTP, 8080);

        component.getDefaultHost().attach("/sample", new SampleApplication());
        OAuth2ServerApplication app = new OAuth2ServerApplication();
        component.getDefaultHost().attach("/oauth", app);
        component.getInternalRouter().attach("/oauth", app);

        component.start();
    }
}
