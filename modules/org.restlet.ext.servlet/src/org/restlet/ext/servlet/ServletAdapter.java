/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.servlet;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.HttpResponse;
import org.restlet.engine.adapter.ServerAdapter;
import org.restlet.ext.servlet.internal.ServletCall;
import org.restlet.ext.servlet.internal.ServletLogger;
import org.restlet.routing.Router;

/**
 * HTTP adapter from Servlet calls to Restlet calls. This class can be used in
 * any Servlet, just create a new instance and override the service() method in
 * your Servlet to delegate all those calls to this class's service() method.
 * Remember to set the next {@link Restlet}, for example using a {@link Router}
 * instance. You can get the Restlet context directly on instances of this
 * class, it will be based on the parent Servlet's context for logging purpose.<br>
 * <br>
 * This class is especially useful when directly integrating Restlets with
 * Spring managed Web applications. Here is a simple usage example:
 * 
 * <pre>
 * public class TestServlet extends HttpServlet {
 *     private ServletAdapter adapter;
 * 
 *     public void init() throws ServletException {
 *         super.init();
 *         this.adapter = new ServletAdapter(getServletContext());
 * 
 *         Restlet trace = new Restlet(this.adapter.getContext()) {
 *             public void handle(Request req, Response res) {
 *                 getLogger().info(&quot;Hello World&quot;);
 *                 res.setEntity(&quot;Hello World!&quot;, MediaType.TEXT_PLAIN);
 *             }
 *         };
 * 
 *         this.adapter.setNext(trace);
 *     }
 * 
 *     protected void service(HttpServletRequest req, HttpServletResponse res)
 *             throws ServletException, IOException {
 *         this.adapter.service(req, res);
 *     }
 * }
 * </pre>
 * 
 * @author Jerome Louvel
 */
public class ServletAdapter extends ServerAdapter {

    /** The next Restlet. */
    private volatile Restlet next;

    /**
     * Constructor. Remember to manually set the "target" property before
     * invoking the service() method.
     * 
     * @param context
     *            The Servlet context.
     */
    public ServletAdapter(ServletContext context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The Servlet context.
     * @param next
     *            The next Restlet.
     */
    public ServletAdapter(ServletContext context, Restlet next) {
        // [ifndef gae] instruction
        super(new Context(new ServletLogger(context)));
        // [ifdef gae] instruction uncomment
        // super(new Context());
        this.next = next;
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
        final String basePath = request.getContextPath()
                + request.getServletPath();
        final String baseUri = request.getRequestURL().toString();
        // Path starts at first slash after scheme://
        final int pathStart = baseUri.indexOf("/",
                request.getScheme().length() + 3);
        if (basePath.length() == 0) {
            // basePath is empty in case the webapp is mounted on root context
            if (pathStart != -1) {
                result = new Reference(baseUri.substring(0, pathStart));
            } else {
                result = new Reference(baseUri);
            }
        } else {
            if (pathStart != -1) {
                final int baseIndex = baseUri.indexOf(basePath, pathStart);
                if (baseIndex != -1) {
                    result = new Reference(baseUri.substring(0, baseIndex
                            + basePath.length()));
                }
            }
        }

        return result;
    }

    /**
     * Returns the next Restlet.
     * 
     * @return The next Restlet.
     */
    public Restlet getNext() {
        return this.next;
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
     * Services a HTTP Servlet request as a Restlet request handled by the
     * "target" Restlet.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @param response
     *            The HTTP Servlet response.
     */
    public void service(HttpServletRequest request, HttpServletResponse response) {
        if (getNext() != null) {
            try {
                // Set the current context
                Context.setCurrent(getContext());

                // Convert the Servlet call to a Restlet call
                ServletCall servletCall = new ServletCall(request
                        .getLocalAddr(), request.getLocalPort(), request,
                        response);
                HttpRequest httpRequest = toRequest(servletCall);
                HttpResponse httpResponse = new HttpResponse(servletCall,
                        httpRequest);

                // Adjust the relative reference
                httpRequest.getResourceRef().setBaseRef(getBaseRef(request));

                // Adjust the root reference
                httpRequest.setRootRef(getRootRef(request));

                // Handle the request and commit the response
                getNext().handle(httpRequest, httpResponse);
                commit(httpResponse);
            } finally {
                Engine.clearThreadLocalVariables();
            }
        } else {
            getLogger().warning("Unable to find the Restlet target");
        }
    }

    /**
     * Sets the next Restlet.
     * 
     * @param next
     *            The next Restlet.
     */
    public void setNext(Restlet next) {
        this.next = next;
    }

    /**
     * Converts a low-level Servlet call into a high-level Restlet request. In
     * addition to the parent {@link ServerAdapter}, it also copies the
     * Servlet's request attributes into the Restlet's request attributes map.
     * 
     * @param servletCall
     *            The low-level Servlet call.
     * @return A new high-level uniform request.
     */
    @SuppressWarnings("unchecked")
    public HttpRequest toRequest(ServletCall servletCall) {
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
