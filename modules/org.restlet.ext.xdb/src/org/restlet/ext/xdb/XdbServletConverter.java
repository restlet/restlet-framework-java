/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.xdb;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.engine.http.HttpRequest;
import org.restlet.engine.http.HttpResponse;
import org.restlet.engine.http.adapter.ServerAdapter;
import org.restlet.ext.servlet.internal.ServletLogger;
import org.restlet.ext.xdb.internal.XdbServletCall;

/**
 * HTTP converter from Servlet calls to Restlet calls. This class can be used in
 * any Servlet, just create a new instance and override the service() method in
 * your Servlet to delegate all those calls to this class's service() method.
 * Remember to set the target Restlet, for example using a Restlet Router
 * instance. You can get the Restlet context directly on instances of this
 * class, it will be based on the parent Servlet's context for logging purpose. <br>
 * <br>
 * This class is especially useful when directly integrating Restlets with
 * Spring managed Web applications. Here is a simple usage example:
 * 
 * <pre>
 * public class TestServlet extends HttpServlet {
 *     private ServletConverter converter;
 * 
 *     public void init() throws ServletException {
 *         super.init();
 *         this.converter = new XDBServletConverter(getServletContext());
 * 
 *         Restlet trace = new Restlet(this.converter.getContext()) {
 *             public void handle(Request req, Response res) {
 *                 getLogger().info(&quot;Hello World&quot;);
 *                 res.setEntity(&quot;Hello World!&quot;, MediaType.TEXT_PLAIN);
 *             }
 *         };
 * 
 *         this.converter.setTarget(trace);
 *     }
 * 
 *     protected void service(HttpServletRequest req, HttpServletResponse res)
 *             throws ServletException, IOException {
 *         this.converter.service(req, res);
 *     }
 * }
 * </pre>
 * 
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 * @deprecated Use {@link XdbServletAdapter} instead
 */
@Deprecated
public class XdbServletConverter extends ServerAdapter {
    /** The target Restlet. */
    private volatile Restlet target;

    /** Connection to the XMLDB repository. */
    private volatile Connection conn;

    /** The local address of the server connector. */
    private volatile String localAddress = null;

    /** The local port of the server connector. */
    private volatile int localPort = -1;

    /**
     * Constructor. Remembers to manually set the "target" property before
     * invoking the service() method.
     * 
     * @param context
     *            The Servlet context.
     */
    public XdbServletConverter(ServletContext context) {
        this(context, null);
    }

    /**
     * Constructor. Remembers to manually set the "target" property before
     * invoking the service() method.
     * 
     * @param context
     *            The Servlet context.
     * @param target
     *            The Restlet target.
     */
    public XdbServletConverter(ServletContext context, Restlet target) {
        super(new Context(new ServletLogger(context)));
        this.target = target;
        CallableStatement preparedstatement = null;
        try {
            this.conn = XdbServerServlet.getConnection();
            int endPoint = 1;
            preparedstatement = this.conn
                    .prepareCall("{ call dbms_xdb.getListenerEndPoint(1,?,?,?) }");
            preparedstatement.registerOutParameter(1, Types.VARCHAR);
            preparedstatement.registerOutParameter(2, Types.INTEGER);
            preparedstatement.registerOutParameter(3, Types.INTEGER);
            preparedstatement.execute();
            this.localAddress = preparedstatement.getString(1);
            if (this.localAddress == null)
                this.localAddress = "127.0.0.1";
            this.localPort = preparedstatement.getInt(2);
            endPoint = preparedstatement.getInt(3);
            getLogger().info(
                    "[Noelios Restlet Engine] - The ServerServlet address = "
                            + this.localAddress + " port = " + this.localPort
                            + " endPoint = " + endPoint);
        } catch (ServletException e) {
            context.log("Failed to get SQL Connection", e);
        } catch (SQLException s) {
            context.log("Failed to get Listener Endpoint", s);
        } finally {
            XdbServerServlet.closeDbResources(preparedstatement, null);
        }
    }

    /**
     * Returns the base reference of new Restlet requests.
     * 
     * @param request
     *            The Servlet request.
     * @return The base reference of new Restlet requests.
     */
    public Reference getBaseRef(HttpServletRequest request) {
        Reference result = null;

        // Do not use getContextPath and getRequestURL,
        // because XMLDB allways returns null and is servlet 2.2
        final String requestUrl = request.getServletPath()
                + request.getRequestURI();
        result = new Reference(requestUrl);
        return result;
    }

    /**
     * Returns the root reference of new Restlet requests. By default it returns
     * the result of getBaseRef().
     * 
     * @param request
     *            The Servlet request.
     * @return The root reference of new Restlet requests.
     */
    public Reference getRootRef(HttpServletRequest request) {
        return getBaseRef(request);
    }

    /**
     * Returns the target Restlet.
     * 
     * @return The target Restlet.
     */
    public Restlet getTarget() {
        return this.target;
    }

    /**
     * Services a HTTP Servlet request as a Restlet request handled by the
     * "target" Restlet.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @param response
     *            The HTTP Servlet response.
     * @throws ServletException
     * @throws IOException
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getTarget() != null) {
            // Set the current context
            Context.setCurrent(getContext());

            // Convert the Servlet call to a Restlet call
            final XdbServletCall servletCall = new XdbServletCall(
                    this.localAddress, this.localPort, request, response);
            final HttpRequest httpRequest = toRequest(servletCall);
            final HttpResponse httpResponse = new HttpResponse(servletCall,
                    httpRequest);

            // Adjust the relative reference
            httpRequest.getResourceRef().setBaseRef(getBaseRef(request));

            // Adjust the root reference
            httpRequest.setRootRef(getRootRef(request));

            // Handle the request and commit the response
            getTarget().handle(httpRequest, httpResponse);
            commit(httpResponse);
        } else {
            getLogger().warning("Unable to find the Restlet target");
        }
    }

    /**
     * Sets the target Restlet.
     * 
     * @param target
     *            The target Restlet.
     */
    public void setTarget(Restlet target) {
        this.target = target;
    }

    /**
     * Converts a low-level Servlet call into a high-level Restlet request. In
     * addition to the parent HttpServerConverter class, it also copies the
     * Servlet's request attributes into the Restlet's request attributes map.
     * 
     * @param servletCall
     *            The low-level Servlet call.
     * @return A new high-level uniform request.
     */
    @SuppressWarnings("unchecked")
    public HttpRequest toRequest(XdbServletCall servletCall) {
        final HttpRequest result = super.toRequest(servletCall);

        // Copy all Servlet's request attributes
        String attributeName;
        for (final Enumeration<String> namesEnum = servletCall.getRequest()
                .getAttributeNames(); namesEnum.hasMoreElements();) {
            attributeName = namesEnum.nextElement();
            result.getAttributes().put(attributeName,
                    servletCall.getRequest().getAttribute(attributeName));
        }

        return result;
    }

}
