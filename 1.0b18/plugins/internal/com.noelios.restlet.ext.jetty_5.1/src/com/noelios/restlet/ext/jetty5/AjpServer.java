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

import org.mortbay.util.InetAddrPort;
import org.restlet.data.Protocol;

/**
 * Jetty AJP server connector.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class AjpServer extends JettyServer
{
   /**
    * Constructor.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AjpServer(String address, int port)
   {
      super(address, port);
      getProtocols().add(Protocol.AJP);
   }

   /** Start hook. */
   public void start() throws Exception
   {
   	AjpListener listener;
   	
      if(getAddress() != null)
      {
         listener = new AjpListener(this, new InetAddrPort(getAddress(), getPort()));
      }
      else
      {
         listener = new AjpListener(this);
         listener.setPort(getPort());
      }

      // Configure the listener
      listener.setMinThreads(getMinThreads());
      listener.setMaxThreads(getMaxThreads());
      listener.setMaxIdleTimeMs(getMaxIdleTimeMs());
      
      setListener(listener);
      super.start();
   }

}
