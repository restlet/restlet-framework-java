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

package org.restlet.test.engine.connector;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.HttpInboundRequest;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.engine.header.Header;
import org.restlet.test.RestletTestCase;
import org.restlet.util.Series;

/**
 * Unit test for inbound HTTP requests with special HTTP headers.
 * 
 * @author Jerome Louvel
 */
public class HttpInboundRequestTestCase extends RestletTestCase {

    public void testRequestUri() throws Exception {
        Engine.register(false);
        Engine.getInstance().getRegisteredServers()
                .add(new HttpServerHelper(null));
        Server server = new Server(new Context(), Protocol.HTTP, 0);
        server.start();

        HttpServerHelper hsh = (HttpServerHelper) server.getContext()
                .getAttributes().get("org.restlet.engine.helper");
        Connection<Server> c = hsh.getConnectionPool().checkout();

        HttpInboundRequest hir = new HttpInboundRequest(server.getContext(), c,
                "GET",
                "/control/accounts/netdev/subscriptions/emily/preferences",
                "HTTP/1.1");
        Series<Header> headers = new Series<Header>(Header.class);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("User-Agent", "");
        hir.setHeaders(headers);

        assertEquals("Target URI",
                "/control/accounts/netdev/subscriptions/emily/preferences", hir
                        .getResourceRef().getPath());
    }

}
