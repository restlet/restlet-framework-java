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

package com.noelios.restlet.ext.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.connector.AbstractClient;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ConditionData;
import org.restlet.data.DefaultStatus;
import org.restlet.data.MediaTypes;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.PreferenceData;
import org.restlet.data.Protocols;
import org.restlet.data.Statuses;

import com.noelios.restlet.Factory;
import com.noelios.restlet.connector.AbstractHttpClientCall;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * HTTP client connector using the HttpUrlConnectionCall. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>chunkLength</td>
 * 		<td>int</td>
 * 		<td>0 (uses HttpURLConnection's default)</td>
 * 		<td>The chunk-length when using chunked encoding streaming mode for output. A value of -1 means chunked encoding is disabled for output.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>followRedirects</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, the protocol will automatically follow redirects. If false, the protocol will not automatically follow redirects.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>allowUserInteraction</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, this URL is being examined in a context in which it makes sense to allow user interactions such as popping up an authentication dialog.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>useCaches</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, the protocol is allowed to use caching whenever it can.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectTimeout</td>
 * 		<td>int</td>
 * 		<td>0</td>
 * 		<td>Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced. 0 means infinite timeout.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>readTimeout</td>
 * 		<td>int</td>
 * 		<td>0</td>
 * 		<td>Sets the read timeout to a specified timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/net/index.html">Networking Features</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClient extends AbstractClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(HttpClient.class.getCanonicalName());

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial properties.
    */
   public HttpClient(Component owner, ParameterList parameters)
   {
   	super(owner, parameters);
      getProtocols().add(Protocols.HTTP);
      getProtocols().add(Protocols.HTTPS);
   }
   
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new HTTP protocol call.
    */
   public AbstractHttpClientCall createCall(String method, String resourceUri, boolean hasInput)
   {
      try
      {
         return new HttpUrlConnectionCall(this, method, resourceUri, hasInput);
      }
      catch(IOException e)
      {
         return null;
      }
   }
   
   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(Call call)
   {
   	AbstractHttpClientCall clientCall = null; 
   	
   	try
   	{
      	// Create a new HTTP client call
      	clientCall = createCall(call.getMethod().getName(), call.getResourceRef().toString(), hasInput(call));

      	// Add the user agent header
      	if(call.getClientName() != null)
      	{
      		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_USER_AGENT, call.getClientName());
      	}
      	else
      	{
      		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_USER_AGENT, Factory.VERSION_HEADER);
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

            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_IF_MATCH, value.toString());
         }
         
         if(condition.getModifiedSince() != null)
         {
            String imsDate = DateUtils.format(condition.getModifiedSince(), DateUtils.FORMAT_RFC_1123[0]);
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_IF_MODIFIED_SINCE, imsDate);
         }
         
         if(condition.getNoneMatch() != null)
         {
            StringBuilder value = new StringBuilder();
            
            for(int i = 0; i < condition.getNoneMatch().size(); i++)
            {
               if(i > 0) value.append(", ");
               value.append(condition.getNoneMatch().get(i).getName());
            }

            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_IF_NONE_MATCH, value.toString());
         }

         if(condition.getUnmodifiedSince() != null)
         {
            String iusDate = DateUtils.format(condition.getUnmodifiedSince(), DateUtils.FORMAT_RFC_1123[0]);
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_IF_UNMODIFIED_SINCE, iusDate);
         }
         
         // Add the cookies
         if(call.getCookies().size() > 0)
         {
            String cookies = CookieUtils.format(call.getCookies());
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_COOKIE, cookies);
         }
         
         // Add the referrer header
         if(call.getReferrerRef() != null)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_REFERRER, call.getReferrerRef().toString());
         }

         // Add the preferences
         PreferenceData pref = call.getPreference();
         if(pref.getMediaTypes().size() > 0)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_ACCEPT, PreferenceUtils.format(pref.getMediaTypes()));
         }
         else
         {
        	 clientCall.getRequestHeaders().add(ConnectorCall.HEADER_ACCEPT, MediaTypes.ALL.getName());
         }
         
         if(pref.getCharacterSets().size() > 0)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_ACCEPT_CHARSET, PreferenceUtils.format(pref.getCharacterSets()));
         }
         
         if(pref.getEncodings().size() > 0)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_ACCEPT_ENCODING, PreferenceUtils.format(pref.getEncodings()));
         }
         
         if(pref.getLanguages().size() > 0)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_ACCEPT_LANGUAGE, PreferenceUtils.format(pref.getLanguages()));
         }

         // Add the security
         ChallengeResponse response = call.getSecurity().getChallengeResponse();
         if(response != null)
         {
            clientCall.getRequestHeaders().add(ConnectorCall.HEADER_AUTHORIZATION, SecurityUtils.format(response));
         }
         
         // Add the custom headers that may have been set by the user
         for(Parameter header : call.getConnectorCall().getRequestHeaders())
         {
            clientCall.getRequestHeaders().add(header.getName(), header.getValue());
         }         

         // Send the input representation
         if(hasInput(call))
         {
         	if(call.getInput().getSize() > 0)
         	{
         		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_CONTENT_LENGTH, Long.toString(call.getInput().getSize()));
         	}
         	
         	if(call.getInput().getMediaType() != null)
         	{
         		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_CONTENT_TYPE, call.getInput().getMediaType().toString());
         	}
         	
         	if(call.getInput().getEncoding() != null)
         	{
         		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_CONTENT_ENCODING, call.getInput().getEncoding().toString());
         	}
         	
         	if(call.getInput().getLanguage() != null)
         	{
         		clientCall.getRequestHeaders().add(ConnectorCall.HEADER_CONTENT_LANGUAGE, call.getInput().getLanguage().toString());
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

         // Get the response status
         call.setStatus(new DefaultStatus(clientCall.getResponseStatusCode(), null, clientCall.getResponseReasonPhrase(), null));
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
         // Get the server address
         call.setServerAddress(clientCall.getResponseAddress());

         // Update the connector call associated with the uniform call
         // so that advanced users can read the response headers, etc.
         call.setConnectorCall(clientCall);
         
         // Extract info from headers
         
         for(Parameter header : clientCall.getResponseHeaders())
         {
            if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_LOCATION))
            {
               call.setOutputRef(header.getValue());
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
         call.setOutput(clientCall.getResponseOutput());
      }
      catch(Exception e)
      {
         logger.log(Level.FINE, "An error occured during the processing of the HTTP response.", e);
         call.setStatus(new DefaultStatus(Statuses.CONNECTOR_ERROR_INTERNAL, "Unable to process the response. " + e.getMessage()));
      }
   }

   /**
    * Returns the chunk-length when using chunked encoding streaming mode for output. 
    * A value of -1 means chunked encoding is disabled for output.
    * @return The chunk-length when using chunked encoding streaming mode for output.
    */
   public int getChunkLength()
   {
   	return Integer.parseInt(getParameters().getFirstValue("chunkLength", "0"));
   }

   /**
    * Indicates if the protocol will automatically follow redirects. 
    * @return True if the protocol will automatically follow redirects.
    */
   public boolean isFollowRedirects()
   {
   	return Boolean.parseBoolean(getParameters().getFirstValue("followRedirects", "false"));
   }
   
   /**
    * Indicates if this URL is being examined in a context in which it makes sense to allow user interactions 
    * such as popping up an authentication dialog. 
    * @return True if it makes sense to allow user interactions.
    */
   public boolean isAllowUserInteraction()
   {
   	return Boolean.parseBoolean(getParameters().getFirstValue("allowUserInteraction", "false"));
   }
   
   /**
    * Indicates if the protocol is allowed to use caching whenever it can.
    * @return True if the protocol is allowed to use caching whenever it can.
    */
   public boolean isUseCaches()
   {
   	return Boolean.parseBoolean(getParameters().getFirstValue("useCaches", "false"));
   }
   
   /**
    * Returns the timeout value, in milliseconds, to be used when opening a communications link to 
    * the resource referenced. 0 means infinite timeout.
    * @return The connection timeout value.
    */
   public int getConnectTimeout()
   {
   	return Integer.parseInt(getParameters().getFirstValue("connectTimeout", "0"));
   }
   
   /**
    * Returns the read timeout value. A timeout of zero is interpreted as an infinite timeout.
    * @return The read timeout value.
    */
   public int getReadTimeout()
   {
   	return Integer.parseInt(getParameters().getFirstValue("readTimeout", "0"));
   }
}
