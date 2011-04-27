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

package org.restlet.test.ext.oauth.test;

import static org.junit.Assert.assertNotNull;

import org.restlet.test.ext.oauth.test.resources.OAuthTestApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class ProviderTest {
    public static Component component;

    public static int serverPort = 8080;

    @BeforeClass
    public static void startServer() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, serverPort);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.RIAP);
        component.getDefaultHost()
                .attach("/oauth", new OAuthTestApplication(0));
        component.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    // Bug in restlet HEAD handling

    @Ignore
    @Test
    public void testWebServerFlow() throws Exception {
        ClientResource cr = new ClientResource("http://localhost:" + serverPort
                + "/oauth/provider");
        Representation r = cr.head();
        assertNotNull(r);
        // assertEquals("Response text test",r.getText(), "TestSuccessful");
        // assertEquals("Response content type test",r.getMediaType(),
        // MediaType.TEXT_HTML);
    }
}
