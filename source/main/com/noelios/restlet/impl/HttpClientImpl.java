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

package com.noelios.restlet.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.AbstractClient;
import org.restlet.connector.ClientCall;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ConditionData;
import org.restlet.data.DefaultEncoding;
import org.restlet.data.DefaultLanguage;
import org.restlet.data.DefaultStatus;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Methods;
import org.restlet.data.Parameter;
import org.restlet.data.PreferenceData;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;
import org.restlet.data.Tag;

import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Implementation of a client HTTP connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClientImpl extends AbstractClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpClientImpl");

   /**
    * Create a new HTTP client connector.
    * @param protocol The protocol to use.
    * @param name The unique connector name.
    */
   public HttpClientImpl(Protocol protocol, String name)
   {
      super(protocol, name);
      System.setProperty("http.keepAlive", "false");
   }
   
   /**
    * Returns the supported protocols. 
    * @return The supported protocols.
    */
   public static List<Protocol> getProtocols()
   {
   	return Arrays.asList(new Protocol[]{Protocols.HTTP, Protocols.HTTPS});
   }
   
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new HTTP protocol call.
    */
   public ClientCall createCall(String method, String resourceUri, boolean hasInput)
   {
      try
      {
         return new HttpClientCallImpl(this, method, resourceUri, hasInput);
      }
      catch(IOException e)
      {
         return null;
      }
   }

   /**
    * Returns the connector's protocol.
    * @return The connector's protocol.
    */
   public Protocol getProtocol()
   {
      return Protocols.HTTP;
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(Call call)
   {
   	ClientCall clientCall = null; 
   	
   	try
   	{
      	// Create a new HTTP client call
      	clientCall = createCall(call.getMethod().getName(), call.getResourceRef().toString(), hasInput(call));

      	// Add the user agent header
      	if(call.getClientName() != null)
      	{
      		clientCall.addRequestHeader(ConnectorCall.HEADER_USER_AGENT, call.getClientName());
      	}
      	else
      	{
      		clientCall.addRequestHeader(ConnectorCall.HEADER_USER_AGENT, FactoryImpl.VERSION_HEADER);
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

            clientCall.addRequestHeader(ConnectorCall.HEADER_IF_MATCH, value.toString());
         }
         
         if(condition.getModifiedSince() != null)
         {
            String imsDate = DateUtils.format(condition.getModifiedSince(), DateUtils.FORMAT_RFC_1123[0]);
            clientCall.addRequestHeader(ConnectorCall.HEADER_IF_MODIFIED_SINCE, imsDate);
         }
         
         if(condition.getNoneMatch() != null)
         {
            StringBuilder value = new StringBuilder();
            
            for(int i = 0; i < condition.getNoneMatch().size(); i++)
            {
               if(i > 0) value.append(", ");
               value.append(condition.getNoneMatch().get(i).getName());
            }

            clientCall.addRequestHeader(ConnectorCall.HEADER_IF_NONE_MATCH, value.toString());
         }

         if(condition.getUnmodifiedSince() != null)
         {
            String iusDate = DateUtils.format(condition.getUnmodifiedSince(), DateUtils.FORMAT_RFC_1123[0]);
            clientCall.addRequestHeader(ConnectorCall.HEADER_IF_UNMODIFIED_SINCE, iusDate);
         }
         
         // Add the cookies
         if(call.getCookies().size() > 0)
         {
            String cookies = CookieUtils.format(call.getCookies());
            clientCall.addRequestHeader(ConnectorCall.HEADER_COOKIE, cookies);
         }
         
         // Add the referrer header
         if(call.getReferrerRef() != null)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_REFERRER, call.getReferrerRef().toString());
         }

         // Add the preferences
         PreferenceData pref = call.getPreference();
         if(pref.getMediaTypes().size() > 0)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_ACCEPT, PreferenceUtils.format(pref.getMediaTypes()));
         }
         else
         {
        	 clientCall.addRequestHeader(ConnectorCall.HEADER_ACCEPT, MediaTypes.ALL.getName());
         }
         
         if(pref.getCharacterSets().size() > 0)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_ACCEPT_CHARSET, PreferenceUtils.format(pref.getCharacterSets()));
         }
         
         if(pref.getEncodings().size() > 0)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_ACCEPT_ENCODING, PreferenceUtils.format(pref.getEncodings()));
         }
         
         if(pref.getLanguages().size() > 0)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_ACCEPT_LANGUAGE, PreferenceUtils.format(pref.getLanguages()));
         }

         // Add the security
         ChallengeResponse response = call.getSecurity().getChallengeResponse();
         if(response != null)
         {
            clientCall.addRequestHeader(ConnectorCall.HEADER_AUTHORIZATION, SecurityUtils.format(response));
         }
         
         // Add the custom headers that may have been set by the user
         for(Parameter header : call.getConnectorCall().getRequestHeaders())
         {
            clientCall.addRequestHeader(header.getName(), header.getValue());
         }         

         // Send the input representation
         if(hasInput(call))
         {
         	if(call.getInput().getSize() > 0)
         	{
         		clientCall.addRequestHeader(ConnectorCall.HEADER_CONTENT_LENGTH, Long.toString(call.getInput().getSize()));
         	}
         	
         	if(call.getInput().getMetadata().getMediaType() != null)
         	{
         		clientCall.addRequestHeader(ConnectorCall.HEADER_CONTENT_TYPE, call.getInput().getMetadata().getMediaType().toString());
         	}
         	
         	if(call.getInput().getMetadata().getEncoding() != null)
         	{
         		clientCall.addRequestHeader(ConnectorCall.HEADER_CONTENT_ENCODING, call.getInput().getMetadata().getEncoding().toString());
         	}
         	
         	if(call.getInput().getMetadata().getLanguage() != null)
         	{
         		clientCall.addRequestHeader(ConnectorCall.HEADER_CONTENT_LANGUAGE, call.getInput().getMetadata().getLanguage().toString());
         	}
         }
   	}
      catch(Exception e)
      {
         logger.log(Level.FINE, "An unexpected error occured during the preparation of the HTTP client call.", e);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_INTERNAL, "Unable to create the HTTP call and its headers. " + e.getMessage()));
      }
         
      try
      {
         // Commit the request headers
         clientCall.sendRequestHeaders();

         // Send the input representation
         if(hasInput(call))
         {
            clientCall.sendRequestInput(call.getInput());
         }
      }
      catch(ConnectException ce)
      {
         logger.log(Level.FINE, "An error occured during the connection to the remote HTTP server.", ce);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_CONNECTION, "Unable to connect to the remote server. " + ce.getMessage()));
      }
      catch(SocketTimeoutException ste)
      {
         logger.log(Level.FINE, "An timeout error occured during the communication with the remote HTTP server.", ste);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_COMMUNICATION, "Unable to complete the HTTP call due to a communication timeout error. " + ste.getMessage()));
      }
      catch(FileNotFoundException fnfe)
      {
         logger.log(Level.FINE, "An unexpected error occured during the sending of the HTTP request.", fnfe);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_INTERNAL, "Unable to find a local file for sending. " + fnfe.getMessage()));
      }
      catch(IOException ioe)
      {
         logger.log(Level.FINE, "An error occured during the communication with the remote HTTP server.", ioe);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_COMMUNICATION, "Unable to complete the HTTP call due to a communication error with the remote server. " + ioe.getMessage()));
      }
      catch(Exception e)
      {
         logger.log(Level.FINE, "An unexpected error occured during the sending of the HTTP request.", e);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_INTERNAL, "Unable to send the HTTP request. " + e.getMessage()));
      }
      
      try
      {
         // Get the response status
         call.setStatus(new DefaultStatus(clientCall.getResponseStatusCode(), null, clientCall.getResponseReasonPhrase(), null));

         // Get the server address
         call.setServerAddress(clientCall.getResponseAddress());

         // Update the connector call associated with the uniform call
         // so that advanced users can read the response headers, etc.
         call.setConnectorCall(clientCall);
         
         // Get the response output
         ContentType contentType = null;
         int size = -1;
         Date expires = null;
         Date lastModified = null;
         Encoding encoding = null;
         Language language = null;
         Tag tag = null;
         
         for(Parameter header : clientCall.getResponseHeaders())
         {
            if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_TYPE))
            {
               contentType = new ContentType(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_LENGTH))
            {
               size = Integer.parseInt(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_EXPIRES))
            {
               expires = clientCall.parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_ENCODING))
            {
               encoding = new DefaultEncoding(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_LANGUAGE))
            {
               language = new DefaultLanguage(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_LAST_MODIFIED))
            {
               lastModified = clientCall.parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_ETAG))
            {
               tag = new Tag(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_LOCATION))
            {
               call.setRedirectionRef(header.getValue());
            }
            else if((header.getName().equalsIgnoreCase(ConnectorCall.HEADER_SET_COOKIE)) ||
                  (header.getName().equalsIgnoreCase(ConnectorCall.HEADER_SET_COOKIE2)))
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
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_WWW_AUTHENTICATE))
            {
               ChallengeRequest request = SecurityUtils.parseRequest(header.getValue());
               call.getSecurity().setChallengeRequest(request);
            }            
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_SERVER))
            {
               call.setServerName(header.getValue());
            }            
         }
         
         // Set the output representation
         Representation output = null;

         if(clientCall.getResponseStream() != null)
         {
         	MediaType mediaType = null;
         	if(contentType != null) mediaType = contentType.getMediaType();
            output = new InputRepresentation(clientCall.getResponseStream(), mediaType);
         }
         else if(clientCall.getResponseChannel() != null)
         {
            output = new ReadableRepresentation(clientCall.getResponseChannel(), contentType.getMediaType());
         }

         if(output != null)
         {
            if(contentType != null) output.getMetadata().setCharacterSet(contentType.getCharacterSet());
            output.setSize(size);               
            output.getMetadata().setEncoding(encoding);
            output.getMetadata().setExpirationDate(expires);
            output.getMetadata().setLanguage(language);
            output.getMetadata().setModificationDate(lastModified);
            output.getMetadata().setTag(tag);
            call.setOutput(output);
         }
      }
      catch(Exception e)
      {
         logger.log(Level.FINE, "An error occured during the processing of the HTTP response.", e);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_INTERNAL, "Unable to process the response. " + e.getMessage()));
      }
   }

   /**
    * Determines if a call has any concrete input.
    * @param call The call to analyze.
    * @return True if the call has any concrete input.
    */
   private boolean hasInput(Call call)
   {
      boolean result = true;
      
      if(call.getMethod().equals(Methods.GET) || call.getMethod().equals(Methods.HEAD) ||
            call.getMethod().equals(Methods.DELETE))
      {
         result = false;
      }
      else
      {
         try
         {
            result = (call.getInput() != null) && ((call.getInput().getStream() != null) || (call.getInput().getChannel() != null));
         }
         catch(IOException e)
         {
            result = false;
         }
      }
      
      return result;
   }

}
