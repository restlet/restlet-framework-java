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

package org.restlet.test.engine.connector;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.nio.HttpServerHelper;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.request.HttpInboundRequest;
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
