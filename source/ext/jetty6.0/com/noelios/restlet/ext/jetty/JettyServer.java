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

import java.net.InetSocketAddress;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.restlet.UniformInterface;
import org.restlet.connector.Server;

/**
 * Jetty connector acting as a HTTP server.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 */
public class JettyServer extends org.mortbay.jetty.Server implements Server
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The name of this REST connector. */
   private String name;

   /** The target of Jetty calls. */
   private UniformInterface target;

   /**
    * Constructor.
    * @param name The unique connector name.
    * @param port The HTTP port number.
    * @param target The target component handling calls.
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
    * Handles a HTTP connection.
    * @param connection The connection to handle.
    */
   public void handle(HttpConnection connection)
   {
      JettyCall call = new JettyCall(connection);
      getTarget().handle(call);
      call.reply();
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
    * Returns the target interface.
    * @return The target interface.
    */
   public UniformInterface getTarget()
   {
      return target;
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
