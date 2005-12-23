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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Iterator;

import org.mortbay.http.HttpConnection;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.UniformCall;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaTypes;
import org.restlet.data.RepresentationMetadata;

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
   protected HttpContext service(HttpRequest request, HttpResponse response) throws HttpException,
         IOException
   {
      try
      {
         UniformCall call = new JettyCall(request, response);
         getJettyConnector().getTarget().handle(call);

         // Set the status code in the response
         if(call.getStatus() != null)
         {
            response.setStatus(call.getStatus().getHttpCode(), call.getStatus().getDescription());
         }

         // Set cookies
         CookieSetting cookieSetting;
         for(Iterator iter = call.getCookieSettings().iterator(); iter.hasNext();)
         {
            cookieSetting = (CookieSetting)iter.next();
            response.addSetCookie(new JettyCookie(cookieSetting));
         }

         if((response.getStatus() == HttpResponse.__201_Created)
               || (response.getStatus() == HttpResponse.__300_Multiple_Choices)
               || (response.getStatus() == HttpResponse.__301_Moved_Permanently)
               || (response.getStatus() == HttpResponse.__302_Moved_Temporarily)
               || (response.getStatus() == HttpResponse.__303_See_Other) || (response.getStatus() == 307))
         {
            // Extract the redirection URI from the call output
            if((call.getOutput() != null)
                  && (call.getOutput().getMetadata().getMediaType().equals(MediaTypes.TEXT_URI)))
            {
               response.setField(HttpFields.__Location, call.getOutput().toString());
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

               response.setContentType(contentType.toString());
            }

            if(meta.getExpirationDate() != null)
            {
               response.addDateField("Expires", meta.getExpirationDate());
            }

            if(meta.getModificationDate() != null)
            {
               response.addDateField("Last-Modified", meta.getModificationDate());
            }

            if(meta.getTag() != null)
            {
               response.addField("ETag", meta.getTag().getName());
            }
            
            response.setContentLength((int)call.getOutput().getSize());

            // Send the output to the client
            call.getOutput().write(response.getOutputStream());
         }

         // Commit the response and ensures that all data is flushed out to the
         // caller
         response.commit();

         // Indicates that the request fully handled
         request.setHandled(true);
      }
      catch(Exception re)
      {
         response.setStatus(HttpResponse.__500_Internal_Server_Error);
         request.setHandled(true);
         re.printStackTrace();
      }

      // TOODO
      return null;
   }

   private JettyServer getJettyConnector()
   {
      return (JettyServer)getListener();
   }

}
