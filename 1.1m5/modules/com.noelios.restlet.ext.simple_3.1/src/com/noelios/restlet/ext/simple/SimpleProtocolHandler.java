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

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;

/**
 * Simple protocol handler delegating the calls to the Restlet server helper.
 * 
 * @author Jerome Louvel <a
 *         href="http://www.noelios.com/">Noelios Technologies</a>
 */
public class SimpleProtocolHandler implements ProtocolHandler {
    /** The delegate Restlet server helper. */
    private volatile SimpleServerHelper helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The delegate Restlet server helper.
     */
    public SimpleProtocolHandler(SimpleServerHelper helper) {
        this.helper = helper;
    }

    /**
     * Returns the delegate Restlet server helper.
     * 
     * @return The delegate Restlet server helper.
     */
    public SimpleServerHelper getHelper() {
        return this.helper;
    }

    /**
     * Handles a Simple request/response transaction.
     * 
     * @param request
     *            The Simple request.
     * @param response
     *            The Simple response.
     */
    public void handle(Request request, Response response) {
        getHelper().handle(
                new SimpleCall(getHelper().getHelped(), request, response,
                        getHelper().isConfidential()));

        try {
            // Once the request is handled, the request input stream must be
            // entirely consumed. Not doing so blocks invariably the transaction
            // managed by the SimpleWeb connector.
            final InputStream in = request.getInputStream();
            if (in != null) {
                while (in.read() != -1) {
                    // just consume the stream
                }
            }
        } catch (final IOException e) {
            // This is probably ok, the stream was certainly already
            // closed by the Representation.release() method for
            // example.
            getHelper()
                    .getLogger()
                    .log(
                            Level.FINE,
                            "Exception while consuming the Simple request's input stream",
                            e);
        }

        try {
            response.getOutputStream().close();
        } catch (final IOException e) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.FINE,
                            "Exception while closing the Simple response's output stream",
                            e);
        }
    }

}
