/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jetty;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.connector.HttpServer;
import org.restlet.connector.HttpServerCall;

/**
 * Jetty connector acting as a HTTP server.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 */
public class JettyServer extends org.mortbay.jetty.Server implements HttpServer
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The name of this REST connector. */
   private String name;

   /** The target handler. */
   private UniformInterface target;

   /**
    * Constructor.
    * @param name The unique connector name.
    * @param port The HTTP port number.
    * @param target The target handler.
    */
   public JettyServer(String name, int port, UniformInterface target)
   {
      // Create and configure the Jetty HTTP connector
      Connector connector = new SelectChannelConnector(); // Uses non-blocking NIO
      connector.setPort(port);
      Connector[] connectors = new Connector[]{connector};
      setConnectors(connectors);

      this.name = name;
      this.target = target;
   }

   /**
    * Constructor.
    * @param name The unique connector name.
    * @param address The IP address to listen to.
    * @param target The target component handling calls.
    */
   public JettyServer(String name, InetSocketAddress address, UniformInterface target)
   {
      // Create and configure the Jetty HTTP connector
      Connector connector = new SelectChannelConnector(); // Uses non-blocking NIO
      connector.setHost(address.getHostName());
      connector.setPort(address.getPort());
      Connector[] connectors = new Connector[]{connector};
      setConnectors(connectors);

      this.name = name;
      this.target = target;
   }

   /**
    * @param keystorePath The path of the keystore file.
    * @param keystorePassword The keystore password.
    * @param keyPassword The password of the server key .
    */
   public void configureSSL(String keystorePath, String keystorePassword, String keyPassword)
   {
      throw new IllegalArgumentException("SSL not currently supported by Jetty 6 connector");
   }

   /**
    * Handles a HTTP connection.
    * @param connection The connection to handle.
    */
   public void handle(HttpConnection connection) throws IOException, ServletException
   {
      JettyCall call = new JettyCall(connection);
      getTarget().handle(call);
      call.commitFrom(call);
   }

   /**
    * Handles a uniform call.
    * The default behavior is to as the attached handler to handle the call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      getTarget().handle(call);
   }

   /**
    * Indicates if the connector is stopped.
    * @return True if the connector is stopped.
    */
   public boolean isStopped()
   {
      return !isStarted();
   }

   /**
    * Returns the target handler.
    * @return The target handler.
    */
   public UniformInterface getTarget()
   {
      return this.target;
   }

   /**
    * Sets the target handler.
    * @param target The target handler.
    */
   public void setTarget(UniformInterface target)
   {
      this.target = target;
   }

   /**
    * Handles the HTTP protocol call.<br/>
    * The default behavior is to create an UniformCall and invoke the "handle(UniformCall)" method.
    * @param call The HTTP protocol call.
    */
   public void handle(HttpServerCall call)
   {
      UniformCall uniformCall = call.toUniform();
      handle(uniformCall);
      call.commitFrom(uniformCall);
   }

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Jetty HTTP server";
   }
}
