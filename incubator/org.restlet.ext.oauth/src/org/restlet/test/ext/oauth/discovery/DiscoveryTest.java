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

package org.restlet.test.ext.oauth.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.oauth.experimental.DiscoverableFilter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.ext.oauth.test.resources.OAuthDiscoverableTestApplication;

public class DiscoveryTest {
    public static Component component;

    public static int serverPort = 8080;

    public static OAuthDiscoverableTestApplication client = new OAuthDiscoverableTestApplication();

    @BeforeClass
    public static void startServer() throws Exception {
        Logger log = Context.getCurrentLogger();
        log.info("Starting oauth discovery test!");

        Server server = new Server(Protocol.HTTP, serverPort);
        component = new Component();
        component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);

        component.getDefaultHost().attach("/test", client);

        component.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    @Test
    public void testDiscoverableResource() throws Exception {
        ClientResource cr = new ClientResource("http://localhost:" + serverPort
                + "/test/resource.disc");
        Representation r = cr.options();
        assertNotNull(r);
        assertEquals("Response content type test", r.getMediaType(),
                DiscoverableFilter.MEDIA_TYPE);
        cr.delete();
        cr.release();

        cr = new ClientResource("http://localhost:" + serverPort
                + "/test/resource.wadl");
        r = cr.options();
        assertNotNull(r);
        cr.release();
    }

}
