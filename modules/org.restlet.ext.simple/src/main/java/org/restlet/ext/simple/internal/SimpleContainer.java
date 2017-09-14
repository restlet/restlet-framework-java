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

package org.restlet.ext.simple.internal;

import java.util.logging.Level;

import org.restlet.ext.simple.SimpleServerHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 * Simple container delegating the calls to the Restlet server helper.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions such as
 *             Jetty and Netty, allowing more control at the Restlet API level.
 */
@Deprecated
public class SimpleContainer implements Container {
    /** The delegate Restlet server helper. */
    private volatile SimpleServerHelper helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The delegate Restlet server helper.
     */
    public SimpleContainer(SimpleServerHelper helper) {
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
            response.close();
        } catch (Exception e) {
            getHelper()
                    .getLogger()
                    .log(Level.FINE,
                            "Exception while closing the Simple response's output stream",
                            e);
        }
    }

}
