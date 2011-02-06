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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.security.AccessControlException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.engine.Engine;
import org.restlet.engine.http.ServerCall;
import org.restlet.ext.servlet.ServerServlet;
import org.restlet.ext.xdb.internal.XdbServletCall;
import org.restlet.ext.xdb.internal.XdbServletWarClient;

/**
 * Servlet acting like an HTTP server connector. See <a
 * href="/documentation/1.1/faq#02">Developper FAQ #2</a> for details on how to
 * integrate a Restlet application into a Servlet container.<br/>
 * Here is a sample configuration for your Restlet webapp:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot;?&gt;
 * &lt;!DOCTYPE web-app PUBLIC
 *       &quot;-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN&quot;
 *       &quot;http://java.sun.com/dtd/web-app_2_3.dtd&quot;&gt;
 * &lt;web-app&gt;
 *         &lt;display-name&gt;Restlet adapter&lt;/display-name&gt;
 * 
 * 
 *       &lt;!-- Restlet adapter --&gt;
 *       &lt;servlet&gt;
 *        &lt;servlet-name&gt;XDBServerServlet&lt;/servlet-name&gt;
 *          &lt;servlet-class&gt;
 *              org.restlet.ext.xdb.XDBServerServlet
 *          &lt;/servlet-class&gt;
 *          &lt;!-- Your application class name --&gt;
 *          &lt;init-param
 *            xmlns=&quot;http://xmlns.oracle.com/xdb/xdbconfig.xsd&quot;&gt;
 *            &lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 *            &lt;param-value&gt;
 *               SCOTT:org.restlet.example.tutorial.Part12
 *            &lt;/param-value&gt;
 *            &lt;description&gt;REST Application&lt;/description&gt;
 *          &lt;/init-param&gt;
 *       &lt;/servlet&gt;
 * 
 *       &lt;!-- Catch all requests --&gt;
 *       &lt;servlet-mapping&gt;
 *         &lt;servlet-name&gt;XDBServerServlet&lt;/servlet-name&gt;
 *         &lt;url-pattern&gt;/userapp/*&lt;/url-pattern&gt;
 *       &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * 
 * The enumeration of initParameters of your Servlet will be copied to the
 * "context.parameters" property of your application. This way, you can pass
 * additional initialization parameters to your Restlet application, and share
 * them with existing Servlets.
 * 
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 */
public class XdbServerServlet extends ServerServlet {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Closes JDBC resources
     * 
     * @param statement
     *            Any statement.
     * @param resultSet
     *            Any result set.
     */
    public static void closeDbResources(final Statement statement,
            final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException s) {
                s.printStackTrace(System.err);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException s) {
                s.printStackTrace(System.err);
            }
        }
    }

    /**
     * Returns a JDBC connection. Works inside or outside the OJVM. outside the
     * it uses db.str, db.usr and db.pwd System's properties
     * 
     * @return A JDBC connection.
     * @throws ServletException
     */
    public static Connection getConnection() throws ServletException {
        Connection conn = null;

        if (System.getProperty("java.vm.name").equals("JServer VM")) {
            try {
                conn = DriverManager.getConnection("jdbc:oracle:kprb:",
                        "default", "default");
            } catch (SQLException s) {
                System.err.println("Exception getting SQL Connection: "
                        + s.getLocalizedMessage());
                throw new ServletException(
                        "Unable to connect using: jdbc:oracle:kprb:", s);
            }
        } else {
            Class<?> targetClass;
            try {
                targetClass = Engine
                        .loadClass("oracle.jdbc.driver.OracleDriver");
                final Driver drv = (Driver) targetClass.newInstance();
                DriverManager.registerDriver(drv);
                conn = DriverManager.getConnection("jdbc:oracle:oci:@"
                        + System.getProperty("db.str", "orcl"), System
                        .getProperty("db.usr", "lucene"), System.getProperty(
                        "db.pwd", "lucene"));
            } catch (SQLException s) {
                System.err.println("Exception getting SQL Connection: "
                        + s.getLocalizedMessage());
                throw new ServletException(
                        "Unable to connect using: jdbc:oracle:oci:@"
                                + System.getProperty("db.str", "orcl") + "["
                                + System.getProperty("db.usr", "lucene") + ":"
                                + System.getProperty("db.pwd", "lucene") + "]",
                        s);
            } catch (ClassNotFoundException cnf) {
                System.err
                        .println("Exception getting oracle.jdbc.driver.OracleDriver class: "
                                + cnf.getLocalizedMessage());
            } catch (InstantiationException ie) {
                System.err.println("Exception in Driver.newIntance(): "
                        + ie.getLocalizedMessage());
            } catch (IllegalAccessException iae) {
                System.err.println("Exception in Driver.newIntance(): "
                        + iae.getLocalizedMessage());
            }

        }

        return conn;
    }

    /** Connection to the XMLDB repository. */
    private volatile transient Connection conn;

    /** The local address of the server connector. */
    private volatile String localAddress = null;

    /** The local port of the server connector. */
    private volatile int localPort = -1;

    /** Indicates if remote debugging should be activated. */
    private volatile boolean remoteDebugging = false;

    /**
     * Constructor.
     */
    public XdbServerServlet() {
        super();
    }

    @Override
    protected ServerCall createCall(Server server, HttpServletRequest request,
            HttpServletResponse response) {
        return new XdbServletCall(server, request, response);
    }

    @Override
    protected Client createWarClient(Context appCtx, ServletConfig config) {
        return new XdbServletWarClient(appCtx, config, this.conn);
    }

    @Override
    public void destroy() {
        CallableStatement preparedstatement = null;
        try {
            if (this.remoteDebugging) {
                preparedstatement = this.conn
                        .prepareCall("{ call dbms_debug_jdwp.disconnect }");
                preparedstatement.execute();
            }
        } catch (SQLException e) {
            log(e.getLocalizedMessage(), e);
        } finally {
            closeDbResources(preparedstatement, null);
        }
        super.destroy();
    }

    @Override
    protected Class<?> loadClass(String className)
            throws ClassNotFoundException {
        final int doubleDotPos = className.indexOf(':');
        Class<?> targetClass;

        if (doubleDotPos > 0) {
            final String sch = className.substring(0, doubleDotPos)
                    .toUpperCase();
            final String cName = className.substring(doubleDotPos + 1);
            if (System.getProperty("java.vm.name").equals("JServer VM")) {
                // Use DbmsJava by reflection to avoid dependency to Oracle libs
                // at compiling time
                try {
                    final Class<?> loaderClass = Engine
                            .loadClass("oracle.aurora.rdbms.DbmsJava");
                    final Method meth = loaderClass.getMethod(
                            "classForNameAndSchema", new Class[] {
                                    String.class, String.class });
                    log("[Restlet Framework] - Schema: " + sch + " class: "
                            + cName + " loader: " + loaderClass);
                    targetClass = (Class<?>) meth.invoke(null, new Object[] {
                            cName, sch });
                } catch (NoSuchMethodException nse) {
                    log(
                            "[Restlet Framework] - Could not instantiate a class using SCHEMA: "
                                    + sch + " and class: " + cName, nse);
                    targetClass = Engine.loadClass(className);
                } catch (IllegalAccessException iae) {
                    log(
                            "[Restlet Framework] - Could not instantiate a class using SCHEMA: "
                                    + sch + " and class: " + cName, iae);
                    targetClass = Engine.loadClass(className);
                } catch (InvocationTargetException ite) {
                    log(
                            "[Restlet Framework] - Could not instantiate a class using SCHEMA: "
                                    + sch + " and class: " + cName, ite);
                    targetClass = Engine.loadClass(className);
                } catch (AccessControlException ace) {
                    log(
                            "[Restlet Framework] - Could not instantiate a class using oracle.aurora.rdbms.DbmsJava "
                                    + sch + " and class: " + cName, ace);
                    targetClass = Engine.loadClass(className);
                }
            } else {
                targetClass = Engine.loadClass(className);
            }
        } else { // Not running inside OJVM, may be outside testing
            targetClass = Engine.loadClass(className);
        }

        return targetClass;
    }

    @Override
    public String getInitParameter(String name, String defaultValue) {
        String result = null;

        // XDB do not support Servlet Context parameter
        // use Servlet init parameter instead
        result = this.getInitParameter(name);

        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    @Override
    public void init() throws ServletException {
        CallableStatement preparedstatement = null;
        if (this.conn == null) {
            this.conn = getConnection();
        }

        try {
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

            log("[Noelios Restlet Engine] - The ServerServlet address = "
                    + this.localAddress);
            log("[Noelios Restlet Engine] - The ServerServlet port = "
                    + this.localPort);
            log("[Noelios Restlet Engine] - The ServerServlet endpoint = "
                    + endPoint);
        } catch (SQLException e) {
            log(e.getLocalizedMessage(), e);
        } finally {
            closeDbResources(preparedstatement, null);
        }

        try {
            if (this.remoteDebugging) {
                preparedstatement = this.conn
                        .prepareCall("{ call dbms_debug_jdwp.connect_tcp(?,?) }");
                preparedstatement.setString(1, "localhost");
                preparedstatement.setInt(2, 4000);
                preparedstatement.execute();
            }
        } catch (SQLException e) {
            log(e.getLocalizedMessage(), e);
        } finally {
            closeDbResources(preparedstatement, null);
        }

        try {
            String bootstrapClassName = (System.getProperty("java.vm.name")
                    .equals("JServer VM")) ? "RESTLET:org.restlet.ext.xdb.XdbServerServlet"
                    : "org.restlet.ext.xdb.XdbServerServlet";
            Engine.getInstance().setUserClassLoader(
                    this.loadClass(bootstrapClassName).getClassLoader());
            Application app = getApplication();

            if ((app != null) && (app.isStopped())) {
                try {
                    app.start();
                } catch (Exception e) {
                    log("Error during the starting of the Restlet Application",
                            e);
                }
            }
        } catch (AccessControlException ace) {
            log("Error loading Restlet Application", ace);
            throw new ServletException("Error loading Restlet application", ace);
        } catch (final ClassNotFoundException cne) {
            log("Error loading Restlet Application", cne);
            throw new ServletException("Error loading Restlet application", cne);
        }
    }

    @Override
    protected String getContextPath(HttpServletRequest request) {
        return "";
    }

    @Override
    protected String getLocalAddr(HttpServletRequest request) {
        return this.localAddress;
    }

    @Override
    protected int getLocalPort(HttpServletRequest request) {
        return this.localPort;
    }
}
