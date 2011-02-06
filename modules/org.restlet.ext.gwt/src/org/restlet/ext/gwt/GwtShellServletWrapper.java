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

package org.restlet.ext.gwt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.engine.Engine;
import org.restlet.ext.servlet.ServerServlet;

/**
 * Servlet to Restlet adapter aware of GWT requests. To allow Restlet to handle
 * all non-module requests under the hosted mode of Google Web Toolkit, this
 * wrapper can be used in place of the GWTShellServlet supplied with Google Web
 * Toolkit.
 * <p>
 * As GWTShellServlet expects, add a context parameter called "module" that
 * specifies the fully qualified name of a GWT module. Requests directed to the
 * module will be forwarded to GWTShellServlet; Restlet will handle all other
 * requests.
 * <p>
 * Because this wrapper extends Restlet's ServerServlet, it also supports the
 * parameters for component and application initialization described in the
 * ServerServlet Javadocs.
 * 
 * @see org.restlet.ext.servlet.ServerServlet
 * @author Rob Heittman (rob.heittman@solertium.com)
 * @deprecated The hosted mode has been removed in GWT 2.0
 */
@Deprecated
public class GwtShellServletWrapper extends ServerServlet {

    /** Fulfill serialization contract of servlets. */
    private static final long serialVersionUID = 1L;

    /** Name of the GWT module specified in the servlet configuration. */
    private volatile transient String gwtModule;

    /** The GWT Shell Servlet to composite for GWT module support. */
    private volatile transient Object gwtShellServlet;

    /** The service method for the GWT Shell Servlet. */
    private volatile transient Method gwtShellServletServiceMethod;

    /** Signify whether GWT module support is enabled. */
    private volatile transient boolean gwtSupported = false;

    /**
     * Default constructor.
     */
    public GwtShellServletWrapper() {
        super();
    }

    @Override
    public void destroy() {
        if (this.gwtSupported) {
            destroyGWTShellServlet();
        }

        super.destroy();
    }

    /**
     * Destroy the GWT Shell Servlet using reflection.
     */
    private void destroyGWTShellServlet() {
        try {
            this.gwtShellServlet.getClass().getMethod("destroy").invoke(
                    this.gwtShellServlet);
        } catch (IllegalAccessException x) {
            log("[Noelios Restlet Engine] - Unable to destroy GWTShellServlet",
                    x);
        } catch (InvocationTargetException x) {
            log("[Noelios Restlet Engine] - Unable to destroy GWTShellServlet",
                    x);
        } catch (NoSuchMethodException x) {
            log("[Noelios Restlet Engine] - Unable to destroy GWTShellServlet",
                    x);
        }
    }

    @Override
    public void init() throws ServletException {
        this.gwtModule = getInitParameter("module");

        if (this.gwtModule != null) {
            this.gwtSupported = true;
            instantiateGWTShellServlet();
            initGWTShellServlet(getServletConfig());
        }

        super.init();
    }

    /**
     * Initialize the GWT Shell Servlet using reflection.
     */
    private void initGWTShellServlet(ServletConfig servletConfig) {
        try {
            this.gwtShellServlet.getClass().getMethod("init",
                    ServletConfig.class).invoke(this.gwtShellServlet,
                    servletConfig);
        } catch (IllegalAccessException x) {
            log("[Noelios Restlet Engine] - Unable to init GWTShellServlet", x);
        } catch (InvocationTargetException x) {
            log("[Noelios Restlet Engine] - Unable to init GWTShellServlet", x);
        } catch (NoSuchMethodException x) {
            log("[Noelios Restlet Engine] - Unable to init GWTShellServlet", x);
        }
    }

    /**
     * Instantiates the GWT Shell Servlet using reflection.
     */
    private void instantiateGWTShellServlet() {
        try {
            this.gwtShellServlet = Engine.loadClass(
                    "com.google.gwt.dev.shell.GWTShellServlet").newInstance();
            this.gwtShellServletServiceMethod = this.gwtShellServlet.getClass()
                    .getMethod("service", ServletRequest.class,
                            ServletResponse.class);
        } catch (IllegalAccessException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to instantiate GWTShellServlet",
                    x);
        } catch (ClassNotFoundException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to instantiate GWTShellServlet",
                    x);
        } catch (InstantiationException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to instantiate GWTShellServlet",
                    x);
        } catch (NoSuchMethodException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to find service method for GWTShellServlet",
                    x);
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (this.gwtSupported) {
            final String path = request.getPathInfo();

            if (path.startsWith("/" + this.gwtModule)) {
                serviceGWTShellServlet(request, response);
                return;
            }
        }
        super.service(request, response);
    }

    /**
     * Service the GWT Shell Servlet using reflection.
     */
    private void serviceGWTShellServlet(ServletRequest request,
            ServletResponse response) {
        try {
            this.gwtShellServletServiceMethod.invoke(this.gwtShellServlet,
                    request, response);
        } catch (IllegalAccessException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to call service(request,response) in GWTShellServlet",
                    x);
        } catch (InvocationTargetException x) {
            log(
                    "[Noelios Restlet Engine] - Unable to call service(request,response) in GWTShellServlet",
                    x);
        }
    }

}
