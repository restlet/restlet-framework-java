/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.adapter;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.restlet.Server;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.engine.connector.HttpsServerHelper;
import org.restlet.engine.connector.JettyServerHelper;

/**
 * Jetty handler that knows how to convert Jetty calls into Restlet calls. This handler isn't a full
 * server, if you use it, you need to manually set up the Jetty server connector and add this
 * handler to a Jetty server.
 *
 * @author Valdis Rigdon
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class JettyHandler extends Handler.Abstract {

    /** The Restlet server helper. */
    private final JettyServerHelper helper;

    /**
     * Constructor for HTTP server connectors.
     *
     * @param server Restlet HTTP server connector.
     */
    public JettyHandler(Server server) {
        this(server, false);
    }

    /**
     * Constructor for HTTP server connectors.
     *
     * @param server Restlet server connector.
     * @param secure Indicates if the server supports HTTP or HTTPS.
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
     * Handles a Jetty call by converting it to a Restlet call and giving it for processing to the
     * Restlet server.
     *
     * @param request The Jetty request.
     * @param response The Jetty response.
     * @param callback The Jetty callback.
     */
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        JettyServerCall httpCall =
                new JettyServerCall(this.helper.getHelped(), request, response, callback);
        this.helper.handle(httpCall);
        return true;
    }
}
