/**
 * Copyright 2005-2013 Restlet S.A.S.
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

package org.restlet.test.ext.oauth.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.memory.MemoryClientManager;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ClientManagerTest {

    private static ClientManager clients;

    private static String clientIdToFind;

    private static String clientIdToDelete;

    @BeforeClass
    public static void setupClientManager() {
        clients = new MemoryClientManager();
    }

    @Test
    public void test() {
        // Tests should be done by this order.
        testCreateClient();
        testCreateIllegalClient();
        testDeleteClient();
        testFindById();
    }

    public void testCreateClient() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Client.PROPERTY_APPLICATION_NAME, "Test1");
        Client client1 = clients.createClient(ClientType.PUBLIC,
                new String[] { "http://example.com/cb" }, properties);
        clientIdToFind = client1.getClientId();

        Client client2 = clients.createClient(ClientType.CONFIDENTIAL, null,
                null);
        clientIdToDelete = client2.getClientId();
    }

    public void testCreateIllegalClient() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Client.PROPERTY_APPLICATION_NAME, "Test2");
        properties.put(Client.PROPERTY_SUPPORTED_FLOWS,
                new Object[] { ResponseType.token });
        try {
            clients.createClient(ClientType.CONFIDENTIAL, null, properties);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    public void testDeleteClient() {
        clients.deleteClient(clientIdToDelete);
        Client client = clients.findById(clientIdToDelete);
        assertNull(client);
    }

    public void testFindById() {
        Client client = clients.findById(clientIdToFind);
        assertEquals(clientIdToFind, client.getClientId());
        assertArrayEquals(new String[] { "http://example.com/cb" },
                client.getRedirectURIs());
        assertEquals("Test1",
                client.getProperties().get(Client.PROPERTY_APPLICATION_NAME));
    }
}
