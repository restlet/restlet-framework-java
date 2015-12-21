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

package org.restlet.ext.netty;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;

/**
 * Netty HTTP server connector.
 * 
 * @see <a href="http://netty.io/">Netty home page</a>
 * @author Jerome Louvel
 */
public class HttpServerHelper extends NettyServerHelper {

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    public static void main(String[] args) throws Exception {
        Engine.getInstance().getRegisteredServers()
                .add(0, new HttpServerHelper(null));
        Server server = new Server(Protocol.HTTP, 8080);
        server.setNext(new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                super.handle(request, response);
                response.setEntity("Hello, world!", MediaType.TEXT_PLAIN);
            }
        });

        server.start();
    }

}
