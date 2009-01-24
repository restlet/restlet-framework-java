/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.simple;

import java.util.logging.Level;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 * Simple container delegating the calls to the Restlet server helper.
 * 
 * @author Jerome Louvel
 */
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
                    .log(
                            Level.FINE,
                            "Exception while closing the Simple response's output stream",
                            e);
        }
    }

}
