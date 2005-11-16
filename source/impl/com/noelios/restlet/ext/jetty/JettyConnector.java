/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.ext.jetty;

import java.io.IOException;
import java.net.Socket;

import org.mortbay.http.HttpConnection;
import org.mortbay.http.SocketListener;
import org.mortbay.util.InetAddrPort;
import org.restlet.UniformInterface;
import org.restlet.connector.HttpServer;

/**
 * Jetty connector acting as a HTTP server.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 */
public class JettyConnector extends SocketListener implements HttpServer
{
   private static final long serialVersionUID = 1L;

   /** The name of this REST connector. */
   private String name;

   /** The target of Jetty calls. */
   private UniformInterface target;
   
   /**
    * Constructor.
    * @param name 	The unique connector name.
    * @param port		The HTTP port number.
    * @param target 	The target component handling calls.
    */
   public JettyConnector(String name, int port, UniformInterface target)
   {
      setPort(port);
      this.name = name;
      this.target = target;
   }

   /**
    * Constructor.
    * @param name    The unique connector name.
    * @param address The IP address to listen to.
    * @param target  The target component handling calls.
    */
   public JettyConnector(String name, InetAddrPort address, UniformInterface target)
   {
       super(address);
       this.name = name;
       this.target = target;
   }
   
   /** Start hook. */
   public void start()
   {
      try
      {
         super.start();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /** Stop hook. */
   public void stop()
   {
      try
      {
         super.stop();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /** 
    * Creates an HttpConnection instance. 
    * This method can be used to override the connection instance.
    * @param socket The underlying socket.
    */
   protected HttpConnection createConnection(Socket socket) throws IOException
   {
      return new JettyConnection(this, socket.getInetAddress(),
                                       socket.getInputStream(),
                                       socket.getOutputStream(),
                                       socket);
   }
   
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




