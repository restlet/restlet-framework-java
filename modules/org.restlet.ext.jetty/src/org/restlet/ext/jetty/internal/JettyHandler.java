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

package org.restlet.ext.jetty.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.restlet.Server;
import org.restlet.ext.jetty.HttpServerHelper;
import org.restlet.ext.jetty.HttpsServerHelper;
import org.restlet.ext.jetty.JettyServerHelper;

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
     *            Restlet HTTP server connector.
     */
    public JettyHandler(Server server) {
        this(server, false);
    }

    /**
     * Constructor for HTTP server connectors.
     * 
     * @param server
     *            Restlet server connector.
     * @param secure
     *            Indicates if the server supports HTTP or HTTPS.
     */
    public JettyHandler(Server server, boolean secure) {
        if (secure) {
            this.helper = new HttpsServerHelper(server);
        } else {
            this.helper = new HttpServerHelper(server);
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        this.helper.start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        this.helper.stop();
    }

    /**
     * Handles a Jetty call by converting it to a Restlet call and giving it for
     * processing to the Restlet server.
     * 
     * @param target
     *            The target of the request, either a URI or a name.
     * @param request
     *            The Jetty request.
     * @param servletRequest
     *            The Servlet request.
     * @param servletResponse
     *            The Servlet response.
     */
    public void handle(String target, Request arg1,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws IOException,
            ServletException {
        final Request baseRequest = (servletRequest instanceof Request) ? (Request) servletRequest
                : HttpConnection.getCurrentConnection().getRequest();
        this.helper.handle(new JettyCall(this.helper.getHelped(),
                HttpConnection.getCurrentConnection()));
        baseRequest.setHandled(true);
    }

}
