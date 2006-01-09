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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.mortbay.http.HttpConnection;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * Restlet handler for Jetty HTTP calls.
 */
public class JettyConnection extends HttpConnection
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /**
    * Constructor.
    * @param connector The parent Jetty connector.
    * @param remoteAddress The address of the remote end or null.
    * @param in Input stream to read the request from.
    * @param out Output stream to write the response to.
    * @param connection The underlying connection object.
    */
   public JettyConnection(JettyServer connector, InetAddress remoteAddress, InputStream in, OutputStream out,
         Object connection)
   {
      super(connector, remoteAddress, in, out, connection);
   }

   /**
    * Handle Jetty HTTP calls.
    * @param request The HttpRequest request.
    * @param response The HttpResponse response.
    * @return The HttpContext that completed handling of the request or null.
    * @exception HttpException
    * @exception IOException
    */
   protected HttpContext service(HttpRequest request, HttpResponse response) throws HttpException, IOException
   {
      JettyCall call = new JettyCall(request, response);
      getJettyConnector().getTarget().handle(call);
      call.reply();

      // Commit the response and ensures that all data is flushed out to the caller
      response.commit();

      // Indicates that the request fully handled
      request.setHandled(true);

      return null;
   }

   /**
    * Returns the Jetty connector.
    * @return The Jetty connector.
    */
   private JettyServer getJettyConnector()
   {
      return (JettyServer)getListener();
   }

}
