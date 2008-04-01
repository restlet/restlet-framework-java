/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.Server;

/**
 * Jetty handler that knows how to convert Jetty calls into Restlet calls. This
 * handler isn't a full server, if you use it you need to manually setup the
 * Jetty server connector and add this handler to a Jetty server.
 * 
 * @author Valdis Rigdon
 * @author Jerome Louvel
 */
public class JettyHandler extends AbstractHandler {

    /** The Restlet server helper. */
    private final JettyServerHelper helper;

    /**
     * Constructor for HTTP server connectors.
     * 
     * @param server
     *                Restlet HTTP server connector.
     */
    public JettyHandler(Server server) {
        this(server, false);
    }

    /**
     * Constructor for HTTP server connectors.
     * 
     * @param server
     *                Restlet server connector.
     * @param secure
     *                Indicates if the server supports HTTP or HTTPS.
     */
    public JettyHandler(Server server, boolean secure) {
        if (secure) {
            this.helper = new HttpServerHelper(server);
        } else {
            this.helper = new HttpsServerHelper(server);
        }
    }

    /**
     * Handles a Jetty call by converting it to a Restlet call and giving it for
     * processing to the Restlet server.
     * 
     * @param target
     *                The target of the request, either a URI or a name.
     * @param request
     *                The Jetty request.
     * @param response
     *                The Jetty response.
     * @param dispatch
     *                The Jetty dispatch mode.
     */
    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        Request baseRequest = (request instanceof Request) ? (Request) request
                : HttpConnection.getCurrentConnection().getRequest();
        helper.handle(new JettyCall(helper.getHelped(), HttpConnection
                .getCurrentConnection()));
        baseRequest.setHandled(true);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        helper.start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        helper.stop();
    }

}
