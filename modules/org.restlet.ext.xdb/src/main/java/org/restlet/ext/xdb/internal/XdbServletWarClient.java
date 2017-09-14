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

package org.restlet.ext.xdb.internal;

import java.sql.Connection;
import java.util.List;

import javax.servlet.ServletConfig;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientHelper;

/**
 * Connector acting as a WAR client for a Servlet Application. It internally
 * uses one of the available connectors registered with the current Restlet
 * implementation.<br>
 * <br>
 * Here is an example of WAR URI that can be resolved by this client:
 * "war:///WEB-INF/web.xml" If XdbServerServlet is running with SCOTT's
 * credentials and register with a Servlet Name HelloRestlet a WAR URI will be
 * translated to an XMLDB directory:
 * /home/SCOTT/wars/HelloRestlet/WEB-INF/web.xml
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public class XdbServletWarClient extends Client {
    /** The helper provided by the implementation. */
    private volatile ClientHelper helper;

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The context.
     * @param config
     *            The Servlet config object.
     * @param conn
     *            The JDBC Connection to XMLDB repository.
     */
    public XdbServletWarClient(Context parentContext, ServletConfig config,
            Connection conn) {
        super(parentContext.createChildContext(), (List<Protocol>) null);
        getProtocols().add(Protocol.WAR);
        getProtocols().add(Protocol.FILE);
        this.helper = new XdbServletWarClientHelper(this, config, conn);
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private ClientHelper getHelper() {
        return this.helper;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        getHelper().handle(request, response);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getHelper().start();
    }

    @Override
    public void stop() throws Exception {
        getHelper().stop();
        super.stop();
    }

}
