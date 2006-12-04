/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.util.logging.Level;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;

/**
 * Simple protocol handler delegating the calls to the Restlet server helper.
 * 
 * @author Jerome Louvel (contact@noelios.com) <a
 *         href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class SimpleProtocolHandler implements ProtocolHandler {
    /** The delegate Restlet server helper. */
    private SimpleServerHelper helper;

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
                new SimpleCall(getHelper().getServer(), request, response,
                        getHelper().isConfidential(), getHelper().getServer()
                                .getPort()));

        try {
            response.getOutputStream().close();
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.WARNING,
                            "Exception while closing the Simple response's output stream",
                            ioe);
        }
    }

}
