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

import com.noelios.restlet.application.ApplicationContext;
import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.util.Engine;

/**
 * Servlet acting like an HTTP server connector. See <a
 * href="/documentation/1.0/faq#02">Developper FAQ #2</a> for details on how to
 * integrate a Restlet application into a servlet container.<br/> Here is a
 * sample configuration for your Restlet webapp:
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
 *              com.noelios.restlet.ext.xdb.XDBServerServlet
 *          &lt;/servlet-class&gt;
 *          &lt;!-- Your application class name --&gt;
 *          &lt;init-param
 *            xmlns=&quot;http://xmlns.oracle.com/xdb/xdbconfig.xsd&quot;&gt;
 *            &lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 *            &lt;param-value&gt;
 *               org.restlet.example.tutorial.Part12
 *            &lt;/param-value&gt;
 *            &lt;description&gt;REST Application&lt;/description&gt;
 *          &lt;/init-param&gt;
 *          &lt;init-param
 *            xmlns=&quot;http://xmlns.oracle.com/xdb/xdbconfig.xsd&quot;&gt;
 *            &lt;param-name&gt;org.restlet.query&lt;/param-name&gt;
 *            &lt;param-value&gt;
 *               keywords,kwd,true;xx,yy,false
 *            &lt;/param-value&gt;
 *            &lt;description&gt;
 *              route.extractQuery arguments
 *            &lt;/description&gt;
 *          &lt;/init-param&gt;
 *       &lt;/servlet&gt;
 *
 *       &lt;!-- Catch all requests --&gt;
 *       &lt;servlet-mapping&gt;
 *         &lt;servlet-name&gt;XDBServerServlet&lt;/servlet-name&gt;
 *         &lt;url-pattern&gt;/users/*&lt;/url-pattern&gt;
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
     *                Any statement.
     * @param resultSet
     *                Any result set.
     */
    protected static void closeDbResources(final Statement statement,
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
     * Returns a JDBC connection. Works inside or outside the OJVM.
     *
     * @return A JDBC connection.
     * @throws ServletException
     */
    protected static Connection getConnection() throws ServletException {
        Connection conn = null;

        try {
            if (System.getProperty("java.vm.name").equals("JServer VM")) {
                conn = DriverManager.getConnection("jdbc:oracle:kprb:",
                        "default", "default");
            } else {
                throw new ServletException(
                        "Class designed to be used with Server side driver");
            }
        } catch (SQLException s) {
            System.err.println("Exception getting SQL Connection: "
                    + s.getLocalizedMessage());
            throw new ServletException(
                    "Unable to connect using: jdbc:oracle:kprb:", s);
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
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper result = null;
        Component component = getComponent();
        Application application = getApplication();

        if ((component != null) && (application != null)) {
            // First, let's create a pseudo server
            Server server = new Server(component.getContext(),
                    new ArrayList<Protocol>(), localAddress, localPort,
                    component);
            server.getProtocols().add(Protocol.HTTP);
            result = new HttpServerHelper(server);

            // Attach the application, do not use getServletContext here because
            // XMLDB allways return null
            String uriPattern = request.getServletPath();
            log("[Noelios Restlet Engine] - Attaching application: "
                    + application + " to URI: " + uriPattern);
            component.getDefaultHost().attach(uriPattern, application);
        }

        return result;
    }

    @Override
    public void destroy() {
        CallableStatement preparedstatement = null;
        try {
            if (remoteDebugging) {
                preparedstatement = conn
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

    /**
     * Returns a configuration parameter.
     *
     * @return An String object within the /home/'||USER||'/restlet/app.xml
     *         XMLDB file.
     */
    private String getConfigParameter(String app, String name) {
        String config = null;
        ResultSet resultset = null;
        PreparedStatement preparedstatement = null;
        log("[Noelios Restlet Engine] - Try to load '" + name
                + "' parameter from '/home/'||USER||'" + "/restlet/" + app
                + ".xml");

        try {
            preparedstatement = conn
                    .prepareStatement(
         "select extractValue(res,'/res:Resource/res:Contents/restlet-app/'||?,"
         + "'xmlns:res=http://xmlns.oracle.com/xdb/XDBResource.xsd') from\n"
         + "resource_view where equals_path(res,'/home/'||USER||?)=1");
            preparedstatement.setString(1, name);
            preparedstatement.setString(2, "/restlet/" + app + ".xml");
            resultset = preparedstatement.executeQuery();

            if (resultset.next())
                config = resultset.getString(1);
        } catch (SQLException sqe) {
            log(sqe.getLocalizedMessage(), sqe);
            throw new RuntimeException(
              ".getConfigParameter:  error from XMLDB loading '/home/'||USER||'"
                         + "/restlet/" + app + ".xml", sqe);
        } finally {
            closeDbResources(preparedstatement, resultset);
        }

        return config;
    }

    @Override
    public String getInitParameter(String name, String defaultValue) {
        String app = getServletConfig().getServletName();

        // Try to load from XMLDB repository
        String result = getConfigParameter(app, name);

        // XDB do not support Servlet Context parameter
        // use Servlet init parameter instead
        if (result == null) {
            result = this.getInitParameter(name);
        }

        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    @Override
    public void init() throws ServletException {
        CallableStatement preparedstatement = null;
        if (this.conn == null)
            this.conn = getConnection();

        try {
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

            log("[Noelios Restlet Engine] - The ServerServlet address = "
                    + localAddress);
            log("[Noelios Restlet Engine] - The ServerServlet port = "
                    + localPort);
            log("[Noelios Restlet Engine] - The ServerServlet endpoint = "
                    + endPoint);
        } catch (SQLException e) {
            log(e.getLocalizedMessage(), e);
        } finally {
            closeDbResources(preparedstatement, null);
        }

        try {
            if (remoteDebugging) {
                preparedstatement = conn
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

        if ((getApplication() != null) && (getApplication().isStopped())) {
            try {
                getApplication().start();
            } catch (Exception e) {
                log("Error during the starting of the Restlet Application", e);
            }
        }
    }

    @Override
    protected HttpServerCall createCall(Server server,
            HttpServletRequest request, HttpServletResponse response) {
        return new XdbServletCall(server, request, response);
    }

    @Override
    protected Class<?> getClass(String className)
        throws ClassNotFoundException {
        int doubleDotPos = className.indexOf(':');
        Class<?> targetClass;

        if (doubleDotPos > 0) {
            // Use DbmsJava by reflection to avoid dependency to Oracle libs
            // at compiling time
            String sch = className.substring(0, doubleDotPos);
            String cName = className.substring(doubleDotPos + 1);
            try {
                Class<?> loaderClass = Engine
                        .classForName("oracle.aurora.rdbms.DbmsJava");
                Method meth = loaderClass.getMethod("classForNameAndSchema",
                        new Class[] { String.class, String.class });
                log("[Noelios Restlet Engine] - Schema: " + sch + " class: "
                        + className + " loader: " + loaderClass);
                targetClass = (Class<?>) meth.invoke(null, new Object[] {
                        cName, sch });
            } catch (NoSuchMethodException nse) {
                log(
       "[Noelios Restlet Engine] - Could not instantiate a class using SCHEMA: "
                                + sch + " and class: " + cName, nse);
                targetClass = Engine.classForName(className);
            } catch (IllegalAccessException iae) {
                log(
       "[Noelios Restlet Engine] - Could not instantiate a class using SCHEMA: "
                                + sch + " and class: " + cName, iae);
                targetClass = Engine.classForName(className);
            } catch (InvocationTargetException ite) {
                log(
       "[Noelios Restlet Engine] - Could not instantiate a class using SCHEMA: "
                                + sch + " and class: " + cName, ite);
                targetClass = Engine.classForName(className);
            }
        } else
            targetClass = Engine.classForName(className);
        return targetClass;
    }

    @Override
    protected Client createWarClient(ApplicationContext appCtx,
                                  ServletConfig config) {
        return new XdbServletWarClient(appCtx, config, conn);
    }
}
