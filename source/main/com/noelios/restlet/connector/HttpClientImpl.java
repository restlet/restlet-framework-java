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

package com.noelios.restlet.connector;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.connector.AbstractClient;
import org.restlet.connector.HttpCall;
import org.restlet.connector.HttpClient;
import org.restlet.connector.HttpClientCall;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ConditionData;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.data.PreferenceData;
import org.restlet.data.Representation;
import org.restlet.data.Tag;

import com.noelios.restlet.Engine;
import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;
import com.noelios.restlet.data.StatusImpl;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Implementation of a client connector for the HTTP protocol.
 */
public class HttpClientImpl extends AbstractClient implements HttpClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpClientImpl");

   /**
    * Create a new HTTP client connector.
    * @param name The unique connector name.
    */
   public HttpClientImpl(String name)
   {
      super(name);
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         // Create a new HTTP client call
         HttpClientCall clientCall = createCall(call.getMethod().getName(), call.getResourceRef().toString());

         // Add the user agent header
         if(call.getClientName() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_USER_AGENT, call.getClientName());
         }
         else
         {
            clientCall.addRequestHeader(HttpCall.HEADER_USER_AGENT, Engine.VERSION_HEADER);
         }

         // Add the conditions
         ConditionData condition = call.getCondition(); 
         if(condition.getMatch() != null)
         {
            StringBuilder value = new StringBuilder();
            
            for(int i = 0; i < condition.getMatch().size(); i++)
            {
               if(i > 0) value.append(", ");
               value.append(condition.getMatch().get(i).getName());
            }

            clientCall.addRequestHeader(HttpCall.HEADER_IF_MATCH, value.toString());
         }
         
         if(condition.getModifiedSince() != null)
         {
            String imsDate = DateUtils.format(condition.getModifiedSince(), DateUtils.FORMAT_RFC_1123);
            clientCall.addRequestHeader(HttpCall.HEADER_IF_MODIFIED_SINCE, imsDate);
         }
         
         if(condition.getNoneMatch() != null)
         {
            StringBuilder value = new StringBuilder();
            
            for(int i = 0; i < condition.getNoneMatch().size(); i++)
            {
               if(i > 0) value.append(", ");
               value.append(condition.getNoneMatch().get(i).getName());
            }

            clientCall.addRequestHeader(HttpCall.HEADER_IF_NONE_MATCH, value.toString());
         }

         if(condition.getUnmodifiedSince() != null)
         {
            String iusDate = DateUtils.format(condition.getUnmodifiedSince(), DateUtils.FORMAT_RFC_1123);
            clientCall.addRequestHeader(HttpCall.HEADER_IF_UNMODIFIED_SINCE, iusDate);
         }
         
         // Add the cookies
         if(call.getCookies().size() > 0)
         {
            String cookies = CookieUtils.format(call.getCookies());
            clientCall.addRequestHeader(HttpCall.HEADER_COOKIE, cookies);
         }
         
         // Add the referrer header
         if(call.getReferrerRef() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_REFERRER, call.getReferrerRef().toString());
         }

         // Add the preferences
         PreferenceData pref = call.getPreference();
         if(pref.getMediaTypes() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_ACCEPT, PreferenceUtils.format(pref.getMediaTypes()));
         }
         
         if(pref.getCharacterSets() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_ACCEPT_CHARSET, PreferenceUtils.format(pref.getCharacterSets()));
         }
         
         if(pref.getEncodings() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_ACCEPT_ENCODING, PreferenceUtils.format(pref.getEncodings()));
         }
         
         if(pref.getLanguages() != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_ACCEPT_LANGUAGE, PreferenceUtils.format(pref.getLanguages()));
         }

         // Add the security
         ChallengeResponse response = call.getSecurity().getChallengeResponse();
         if(response != null)
         {
            clientCall.addRequestHeader(HttpCall.HEADER_AUTHORIZATION, SecurityUtils.format(response));
         }
         
         // Commit the request headers
         clientCall.commitRequestHeaders();

         // Send the input representation
         if(call.getInput() != null)
         {
            if(clientCall.getRequestStream() != null)
            {
               call.getInput().write(clientCall.getRequestStream());
            }
            else if(clientCall.getRequestChannel() != null)
            {
               call.getInput().write(clientCall.getRequestChannel());
            }
         }

         // Get the response status
         call.setStatus(new StatusImpl(clientCall.getResponseStatusCode()));

         // Get the server address
         call.setServerAddress(clientCall.getResponseAddress());
         
         // Get the response output
         ContentType contentType = null;
         Date expires = null;
         Date lastModified = null;
         Encoding encoding = null;
         Language language = null;
         Tag tag = null;
         
         Parameter header;
         for(Iterator<Parameter> iter = clientCall.getResponseHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();
            
            if(header.getName().equalsIgnoreCase(HttpCall.HEADER_CONTENT_TYPE))
            {
               contentType = new ContentType(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_EXPIRES))
            {
               expires = clientCall.parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_CONTENT_ENCODING))
            {
               encoding = Manager.createEncoding(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_CONTENT_LANGUAGE))
            {
               language = Manager.createLanguage(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_LAST_MODIFIED))
            {
               lastModified = clientCall.parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_ETAG))
            {
               tag = Manager.createTag(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_LOCATION))
            {
               call.setRedirectRef(Manager.createReference(header.getValue()));
            }
            else if((header.getName().equalsIgnoreCase(HttpCall.HEADER_SET_COOKIE)) ||
                  (header.getName().equalsIgnoreCase(HttpCall.HEADER_SET_COOKIE2)))
            {
               try
               {
                  CookieReader cr = new CookieReader(header.getValue());
                  call.getCookieSettings().add(cr.readCookieSetting());
               }
               catch(Exception e)
               {
                  logger.log(Level.WARNING, "Error during cookie setting parsing. Header: " + header.getValue(), e);
               }
            }
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_WWW_AUTHENTICATE))
            {
               ChallengeRequest request = SecurityUtils.parseRequest(header.getValue());
               call.getSecurity().setChallengeRequest(request);
            }            
            else if(header.getName().equalsIgnoreCase(HttpCall.HEADER_SERVER))
            {
               call.setServerName(header.getValue());
            }            
         }
         
         // Set the output representation
         if(contentType != null)
         {
            Representation output = null;

            if(clientCall.getResponseStream() != null)
            {
               output = new InputRepresentation(clientCall.getResponseStream(), contentType.getMediaType());
            }
            else if(clientCall.getResponseChannel() != null)
            {
               output = new ReadableRepresentation(clientCall.getResponseChannel(), contentType.getMediaType());
            }

            if(output != null)
            {
               if(contentType != null) output.getMetadata().setCharacterSet(contentType.getCharacterSet());
               output.getMetadata().setEncoding(encoding);
               output.getMetadata().setExpirationDate(expires);
               output.getMetadata().setLanguage(language);
               output.getMetadata().setModificationDate(lastModified);
               output.getMetadata().setTag(tag);
               call.setOutput(output);
            }
         }
      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "An error occured during the handling of an HTTP client call.", e);
      }
   }

   /**
    * Returns a new HTTP protocol call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @return A new HTTP protocol call.
    */
   public HttpClientCall createCall(String method, String resourceUri)
   {
      try
      {
         return new HttpClientCallImpl(method, resourceUri);
      }
      catch(IOException e)
      {
         return null;
      }
   }

}
