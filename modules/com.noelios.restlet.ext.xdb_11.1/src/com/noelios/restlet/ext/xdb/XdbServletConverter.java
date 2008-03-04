/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.ext.xdb;

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

import com.noelios.restlet.ext.servlet.ServletLogger;
import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerConverter;

/**
 * HTTP converter from Servlet calls to Restlet calls. This class can be used in
 * any Servlet, just create a new instance and override the service() method in
 * your Servlet to delegate all those calls to this class's service() method.
 * Remember to set the target Restlet, for example using a Restlet Router
 * instance. You can get the Restlet context directly on instances of this
 * class, it will be based on the parent Servlet's context for logging purpose.<br>
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
 */
public class XdbServletConverter extends HttpServerConverter {
    /** The target Restlet. */
    private volatile Restlet target;

    /** Connection to the XMLDB repository. */
    private volatile transient Connection conn;

    /** The local address of the server connector. */
    private volatile transient String localAddress = null;

    /** The local port of the server connector. */
    private volatile transient int localPort = -1;

    /**
     * Constructor. Remembers to manually set the "target" property before
     * invoking the service() method.
     * 
     * @param context
     *                The Servlet context.
     */
    public XdbServletConverter(ServletContext context) {
        this(context, null);
    }

    /**
     * Constructor. Remembers to manually set the "target" property before
     * invoking the service() method.
     * 
     * @param context
     *                The Servlet context.
     */
    public XdbServletConverter(ServletContext context, Restlet target) {
        super(new Context(new ServletLogger(context)));
        this.target = target;
        CallableStatement preparedstatement = null;
        try {
            conn = XdbServerServlet.getConnection();
            @SuppressWarnings("unused")
            int endPoint = 1;
            preparedstatement = conn
                    .prepareCall("{ call dbms_xdb.getListenerEndPoint(1,?,?,?) }");
            preparedstatement.registerOutParameter(1, Types.VARCHAR);
            preparedstatement.registerOutParameter(2, Types.INTEGER);
            preparedstatement.registerOutParameter(3, Types.INTEGER);
            preparedstatement.execute();
            localAddress = preparedstatement.getString(1);
            localPort = preparedstatement.getInt(2);
            endPoint = preparedstatement.getInt(3);
        } catch (ServletException e) {
            context.log("Failed to get SQL Connection", e);
        } catch (SQLException s) {
            context.log("Failed to get Listener Endpoint", s);
        } finally {
            XdbServerServlet.closeDbResources(preparedstatement, null);
        }
    }

    /**
     * Services a HTTP Servlet request as a Restlet request handled by the
     * "target" Restlet.
     * 
     * @param request
     *                The HTTP Servlet request.
     * @param response
     *                The HTTP Servlet response.
     */
    @SuppressWarnings("unused")
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getTarget() != null) {
            // Convert the Servlet call to a Restlet call
            XdbServletCall servletCall = new XdbServletCall(getLogger(),
                    this.localAddress, this.localPort, request, response);
            HttpRequest httpRequest = toRequest(servletCall);
            HttpResponse httpResponse = new HttpResponse(servletCall,
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
     * Converts a low-level Servlet call into a high-level Restlet request. In
     * addition to the parent HttpServerConverter class, it also copies the
     * Servlet's request attributes into the Restlet's request attributes map.
     * 
     * @param servletCall
     *                The low-level Servlet call.
     * @return A new high-level uniform request.
     */
    @SuppressWarnings("unchecked")
    public HttpRequest toRequest(XdbServletCall servletCall) {
        HttpRequest result = super.toRequest(servletCall);

        // Copy all Servlet's request attributes
        String attributeName;
        for (Enumeration<String> namesEnum = servletCall.getRequest()
                .getAttributeNames(); namesEnum.hasMoreElements();) {
            attributeName = namesEnum.nextElement();
            result.getAttributes().put(attributeName,
                    servletCall.getRequest().getAttribute(attributeName));
        }

        return result;
    }

    /**
     * Returns the base reference of new Restlet requests.
     * 
     * @param request
     *                The Servlet request.
     * @return The base reference of new Restlet requests.
     */
    public Reference getBaseRef(HttpServletRequest request) {
        Reference result = null;

        // Do not use getContextPath and getRequestURL,
        // because XMLDB allways returns null and is servlet 2.2
        String requestUrl = request.getServletPath() + request.getRequestURI();
        result = new Reference(requestUrl);
        return result;
    }

    /**
     * Returns the root reference of new Restlet requests. By default it returns
     * the result of getBaseRef().
     * 
     * @param request
     *                The Servlet request.
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
     * Sets the target Restlet.
     * 
     * @param target
     *                The target Restlet.
     */
    public void setTarget(Restlet target) {
        this.target = target;
    }

}
