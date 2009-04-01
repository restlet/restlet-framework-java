/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
     * @param response
     *            The Jetty response.
     * @param dispatch
     *            The Jetty dispatch mode.
     */
    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        final Request baseRequest = (request instanceof Request) ? (Request) request
                : HttpConnection.getCurrentConnection().getRequest();
        this.helper.handle(new JettyCall(this.helper.getHelped(),
                HttpConnection.getCurrentConnection()));
        baseRequest.setHandled(true);
    }

}
