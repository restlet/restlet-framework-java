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

import org.restlet.component.Component;
import org.restlet.data.ParameterList;

import com.noelios.restlet.impl.HttpServer;

/**
 * Abstract Jetty Web server connector.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class JettyServer extends HttpServer
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** The Jetty listener (keep package prefixing). */
   protected org.mortbay.http.HttpListener listener;
  
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public JettyServer(Component owner, ParameterList parameters, String address, int port)
   {
      super(owner, parameters, address, port);
   }

   /**
    * Returns the Jetty listener.
    * @return The Jetty listener.
    */
   public org.mortbay.http.HttpListener getListener()
   {
      return this.listener;
   }

   /** Start connector. */
   public void start() throws Exception
   {
      getListener().start();
      super.start();
   }

   /** Stop connector. */
   public void stop() throws Exception
   {
      getListener().stop();
      super.stop();
   }

}
