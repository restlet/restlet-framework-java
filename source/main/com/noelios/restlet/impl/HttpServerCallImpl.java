/*
 * Copyright 2005-2006 Jerome LOUVEL
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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.ConnectorCall;
import org.restlet.connector.ServerCall;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encodings;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Implementation of a server HTTP call.
 */
public abstract class HttpServerCallImpl extends ConnectorCallImpl implements ServerCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpServerCallImpl");
   
   /**
    * Converts to an uniform call.
    * @return An equivalent uniform call.
    */
   public Call toUniform()
   {
      return new HttpServerRestletCall(this);
   }
   
   /**
    * Sets the response from an uniform call.<br>
    * Sets the response headers and the response status. 
    * @param call The call to update from.
    */
   public void setResponse(Call call)
   {
      try
      {
         // Add the cookie settings
         List<CookieSetting> cookies = call.getCookieSettings();
         for(int i = 0; i < cookies.size(); i++)
         {
            addResponseHeader(ConnectorCall.HEADER_SET_COOKIE, CookieUtils.format(cookies.get(i)));
         }
         
         // Set the redirection URI
         if(call.getRedirectionRef() != null)
         {
            addResponseHeader(HEADER_LOCATION, call.getRedirectionRef().toString());
         }

         // Set the security data
         if(call.getSecurity().getChallengeRequest() != null)
         {
            addResponseHeader(HEADER_WWW_AUTHENTICATE, SecurityUtils.format(call.getSecurity().getChallengeRequest()));
         }

         // Set the server name again
         addResponseHeader(HEADER_SERVER, call.getServerName());
         
         // Set the status code in the response
         if(call.getStatus() != null)
         {
            setResponseStatus(call.getStatus().getHttpCode(), call.getStatus().getDescription());
         }
   
         // If an output was set during the call, copy it to the output stream;
         if(call.getOutput() != null)
         {
            RepresentationMetadata meta = call.getOutput().getMetadata();
   
            if(meta.getExpirationDate() != null)
            {
               addResponseHeader(HEADER_EXPIRES, formatDate(meta.getExpirationDate(), false));
            }
            
            if((meta.getEncoding() != null) && (!meta.getEncoding().equals(Encodings.IDENTITY)))
            {
               addResponseHeader(HEADER_CONTENT_ENCODING, meta.getEncoding().getName());
            }
            
            if(meta.getLanguage() != null)
            {
               addResponseHeader(HEADER_CONTENT_LANGUAGE, meta.getLanguage().getName());
            }
            
            if(meta.getMediaType() != null)
            {
               StringBuilder contentType = new StringBuilder(meta.getMediaType().getName());
   
               if(meta.getCharacterSet() != null)
               {
                  // Specify the character set parameter
                  contentType.append("; charset=").append(meta.getCharacterSet().getName());
               }
   
               addResponseHeader(HEADER_CONTENT_TYPE, contentType.toString());
            }
   
            if(meta.getModificationDate() != null)
            {
               addResponseHeader(HEADER_LAST_MODIFIED, formatDate(meta.getModificationDate(), false));
            }
   
            if(meta.getTag() != null)
            {
               addResponseHeader(HEADER_ETAG, meta.getTag().getName());
            }
            
            if(call.getOutput().getSize() != -1)
            {
               addResponseHeader(HEADER_CONTENT_LENGTH, Long.toString(call.getOutput().getSize()));
            }
         }
      }
      catch(Exception e)
      {
         logger.log(Level.INFO, "Exception intercepted", e);
         setResponseStatus(500, "An unexpected exception occured");
      }
   }

   /**
    * Sends the response output.
    * @param output The response output;
    */
   public void sendResponseOutput(Representation output) throws IOException
   {
      if(output != null)
      {
         // Send the output to the client
         output.write(getResponseStream());
      }
   }
   
   /**
    * Parses a date string.
    * @param date The date string to parse.
    * @param cookie Indicates if the date is in the cookie format.
    * @return The parsed date.
    */
   public Date parseDate(String date, boolean cookie)
   {
      if(cookie)
      {
         return DateUtils.parse(date, DateUtils.FORMAT_RFC_1036);
      }
      else
      {
         return DateUtils.parse(date, DateUtils.FORMAT_RFC_1123);
      }
   }
   
   /**
    * Formats a date as a header string.
    * @param date The date to format.
    * @param cookie Indicates if the date should be in the cookie format.
    * @return The formatted date.
    */
   public String formatDate(Date date, boolean cookie)
   {
      if(cookie)
      {
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1036[0]);
      }
      else
      {
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1123[0]);
      }
   }
   
}

