/*
 * Copyright 2005 Jérôme LOUVEL
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
import java.util.Iterator;
import java.util.logging.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.connector.Server;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaTypes;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.Statuses;

/**
 * Jetty connector acting as a HTTP server.
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 */
public class JettyServer extends org.mortbay.jetty.Server implements Server
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ext.jetty.JettyServer");

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

   public void handle(HttpConnection connection)
   {
      long startTime = System.currentTimeMillis();

      try
      {
         UniformCall call = new JettyCall(connection);
         getTarget().handle(call);

         // Set the status code in the response
         if(call.getStatus() != null)
         {
            connection.getResponse().setStatus(call.getStatus().getHttpCode(), call.getStatus().getDescription());
         }

         // Set cookies
         CookieSetting cookieSetting;
         for(Iterator iter = call.getCookieSettings().iterator(); iter.hasNext();)
         {
            cookieSetting = (CookieSetting)iter.next();
            connection.getResponse().addCookie(new JettyCookie(cookieSetting));
         }

         int status = connection.getResponse().getStatus();
         if((status == Statuses.SUCCESS_CREATED.getHttpCode())
               || (status == Statuses.REDIRECTION_MULTIPLE_CHOICES.getHttpCode())
               || (status == Statuses.REDIRECTION_MOVED_PERMANENTLY.getHttpCode())
               || (status == Statuses.REDIRECTION_MOVED_TEMPORARILY.getHttpCode())
               || (status == Statuses.REDIRECTION_SEE_OTHER.getHttpCode())
               || (status == Statuses.REDIRECTION_TEMPORARY_REDIRECT.getHttpCode()))
         {
            // Extract the redirection URI from the call output
            if((call.getOutput() != null)
                  && (call.getOutput().getMetadata().getMediaType().equals(MediaTypes.TEXT_URI)))
            {
               connection.getResponse().setHeader(HttpHeaders.LOCATION, call.getOutput().toString());
               call.setOutput(null);
            }
         }

         // If an output was set during the call, copy it to the output stream;
         if(call.getOutput() != null)
         {
            RepresentationMetadata meta = call.getOutput().getMetadata();

            if(meta.getMediaType() != null)
            {
               StringBuilder contentType = new StringBuilder(meta.getMediaType().getName());

               if(meta.getCharacterSet() != null)
               {
                  // Specify the character set parameter
                  contentType.append("; charset=").append(meta.getCharacterSet().getName());
               }

               connection.getResponse().setContentType(contentType.toString());
            }

            if(meta.getExpirationDate() != null)
            {
               connection.getResponse().addDateHeader("Expires", meta.getExpirationDate().getTime());
            }

            if(meta.getModificationDate() != null)
            {
               connection.getResponse().addDateHeader("Last-Modified", meta.getModificationDate().getTime());
            }

            if(meta.getTag() != null)
            {
               connection.getResponse().addHeader("ETag", meta.getTag().getName());
            }

            connection.getResponse().setContentLength((int)call.getOutput().getSize());

            // Send the output to the client
            call.getOutput().write(connection.getResponse().getOutputStream());
         }
      }
      catch(Exception re)
      {
         connection.getResponse().setStatus(Statuses.SERVER_ERROR_INTERNAL.getHttpCode());
         re.printStackTrace();
      }

      long endTime = System.currentTimeMillis();
      int duration = (int)(endTime - startTime);
      logger.info("Call duration=" + duration + "ms");
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
