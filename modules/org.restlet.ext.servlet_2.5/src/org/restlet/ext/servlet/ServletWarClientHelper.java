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

package org.restlet.ext.servlet;

import javax.servlet.ServletContext;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.local.Entity;
import org.restlet.engine.local.EntityClientHelper;


/**
 * Client connector based on a Servlet context (JEE Web application context).
 * Here is a sample resource URI:<br>
 * 
 * <pre>
 * war:///path/to/my/resource/entry.txt
 * </pre>
 * 
 * <br>
 * You can note that there is no authority which is denoted by the sequence of
 * three "/" characters. This connector is designed to be used inside a context
 * (e.g. inside a servlet based application) and subconsequently does not
 * require the use of a authority. Such URI are "relative" to the root of the
 * servlet context.<br>
 * Here is a sample code excerpt that illustrates the way to use this connector:
 * 
 * <code>Response response = getContext().getClientDispatcher().get("war:///myDir/test.txt");
 if (response.isEntityAvailable()) {
 //Do what you want to do.
 }
 </code>
 * 
 * @author Jerome Louvel
 */
public class ServletWarClientHelper extends EntityClientHelper {

    /** The Servlet context to use. */
    private volatile ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     * @param servletContext
     *            The Servlet context.
     */
    public ServletWarClientHelper(Client client, ServletContext servletContext) {
        super(client);
        getProtocols().clear();
        getProtocols().add(Protocol.WAR);
        this.servletContext = servletContext;
    }

    @Override
    public Entity getEntity(String decodedPath) {
        return new ServletWarEntity(getServletContext(), decodedPath);
    }

    /**
     * Returns the Servlet context.
     * 
     * @return The Servlet context.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void handle(Request request, Response response) {
        final String scheme = request.getResourceRef().getScheme();
        if (Protocol.WAR.getSchemeName().equalsIgnoreCase(scheme)) {
            super.handle(request, response);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only WAR is supported.");
        }
    }

}
