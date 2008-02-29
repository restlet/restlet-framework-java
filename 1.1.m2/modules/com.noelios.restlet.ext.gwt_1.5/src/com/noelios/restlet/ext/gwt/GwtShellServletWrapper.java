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

package com.noelios.restlet.ext.gwt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.noelios.restlet.ext.servlet.ServerServlet;

/**
 * To allow Restlet to handle all non-module requests under the hosted mode of
 * Google Web Toolkit, this wrapper can be used in place of the GWTShellServlet
 * supplied with Google Web Toolkit.
 * <p>
 * As GWTShellServlet expects, add a context parameter called "module" that
 * specifies the fully qualified name of a GWT module. Requests directed to the
 * module will be forwarded to GWTShellServlet; Restlet will handle all other
 * requests.
 * <p>
 * Because this wrapper extends Restlet's ServerServlet, it also supports the
 * parameters for component and application initialization described in the
 * ServerServlet javadocs.
 * 
 * @see com.noelios.restlet.ext.servlet.ServerServlet
 * @author Rob Heittman (rob.heittman@solertium.com)
 */
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

	public GwtShellServletWrapper() {
		super();
	}

	@Override
	public void destroy() {
		if (gwtSupported) {
			destroyGWTShellServlet();
		}
		super.destroy();
	}

	/**
	 * Destroy the GWT Shell Servlet using reflection.
	 */
	private void destroyGWTShellServlet() {
		try {
			gwtShellServlet.getClass().getMethod("destroy").invoke(
					gwtShellServlet);
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
		gwtModule = getInitParameter("module");
		if (gwtModule != null) {
			gwtSupported = true;
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
			gwtShellServlet.getClass().getMethod("init", ServletConfig.class)
					.invoke(gwtShellServlet, servletConfig);
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
			gwtShellServlet = Class.forName(
					"com.google.gwt.dev.shell.GWTShellServlet").newInstance();
			gwtShellServletServiceMethod = gwtShellServlet.getClass()
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
		if (gwtSupported) {
			String path = request.getPathInfo();
			if (path.startsWith("/" + gwtModule)) {
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
			gwtShellServletServiceMethod.invoke(gwtShellServlet, request,
					response);
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
