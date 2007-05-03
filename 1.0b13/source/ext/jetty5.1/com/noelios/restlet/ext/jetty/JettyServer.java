/*
 * Copyright 2005-2006 Noelios Consulting.
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
import java.util.Arrays;
import java.util.List;

import org.mortbay.util.InetAddrPort;
import org.restlet.connector.AbstractServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

/**
 * Jetty HTTP server connector.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class JettyServer extends AbstractServer
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The Jetty listener. */
   protected org.mortbay.http.HttpListener listener;

   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param delegate The delegate Server.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public JettyServer(Protocol protocol, String name, Server delegate, String address, int port)
   {
      super(protocol, name, delegate, address, port);
   }
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param delegate The delegate Server.
    * @param address The IP address to listen to.
    */
   public JettyServer(Protocol protocol, String name, Server delegate, InetSocketAddress address)
   {
   	this(protocol, name, delegate, address.getHostName(), address.getPort());
   }

   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param delegate The delegate Server.
    * @param port The HTTP port number.
    */
   public JettyServer(Protocol protocol, String name, Server delegate, int port)
   {
   	this(protocol, name, delegate, null, port);
   }
   
   /**
    * Returns the supported protocols. 
    * @return The supported protocols.
    */
   public static List<Protocol> getProtocols()
   {
   	return Arrays.asList(new Protocol[]{Protocols.HTTP, Protocols.HTTPS, Protocols.AJP});
   }

   /**
    * Returns the Jetty listener.
    * @return The Jetty listener.
    */
   public org.mortbay.http.HttpListener getListener()
   {
      return this.listener;
   }

   /** Start hook. */
   public void start()
   {
      try
      {
         if(Protocols.AJP.equals(this.protocol))
         {
            if(this.address != null)
            {
               this.listener = new AjpListener(this, new InetAddrPort(this.address, this.port));
            }
            else
            {
               this.listener = new AjpListener(this);
               this.listener.setPort(port);
            }
         }
         else if(Protocols.HTTP.equals(this.protocol))
         {
            if(this.address != null)
            {
               this.listener = new HttpListener(this, new InetAddrPort(this.address, this.port));
            }
            else
            {
               this.listener = new HttpListener(this);
               this.listener.setPort(port);
            }
         }
         else if(Protocols.HTTPS.equals(this.protocol))
         {
            if(this.address != null)
            {
               HttpsListener httpsListener = new HttpsListener(this,
                     new InetAddrPort(this.address, this.port));
               httpsListener.setKeystore(this.keystorePath);
               httpsListener.setPassword(this.keystorePassword);
               httpsListener.setKeyPassword(this.keyPassword);
               this.listener = httpsListener;
            }
            else
            {
               this.listener = new HttpsListener(this);
               this.listener.setPort(port);
            }
         }

         getListener().start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /** Stop hook. */
   public void stop()
   {
      try
      {
         getListener().stop();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Indicates if the connector is started.
    * @return True if the connector is started.
    */
   public boolean isStarted()
   {
      return getListener().isStarted();
   }

   /**
    * Indicates if the connector is stopped.
    * @return True if the connector is stopped.
    */
   public boolean isStopped()
   {
      return !isStarted();
   }

}
