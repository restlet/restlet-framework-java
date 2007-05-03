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

package com.noelios.restlet.ext.jetty5;

import java.io.IOException;
import java.net.Socket;

import org.mortbay.http.ajp.AJP13Connection;
import org.mortbay.http.ajp.AJP13Listener;
import org.mortbay.util.InetAddrPort;

/**
 * Jetty AJP listener.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class AjpListener extends AJP13Listener
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The parent Jetty server. */
   private JettyServer server;
   
   /**
    * Constructor.
    * @param server The parent Jetty server.
    */
   public AjpListener(JettyServer server)
   {
      this.server = server;
   }

   /**
    * Constructor.
    * @param server The parent Jetty server.
    * @param address The listening address.
    */
   public AjpListener(JettyServer server, InetAddrPort address)
   {
      super(address);
      this.server = server;
   }
   
   /**
    * Returns the parent Jetty server.
    * @return The parent Jetty server.
    */
   public JettyServer getServer()
   {
      return this.server;
   }
   
   /** 
    * Creates an AJP13Connection instance. 
    * This method can be used to override the connection instance.
    * @param socket The underlying socket.
    * @return The created connection.
    */
   protected AJP13Connection createConnection(Socket socket) throws IOException
   {
       return new AjpConnection(this, socket.getInputStream(), socket.getOutputStream(), socket, getBufferSize());
   }

}
