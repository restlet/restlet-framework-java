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

package com.noelios.restlet.connector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.Connector;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.Methods;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.SecurityData;
import org.restlet.data.Statuses;
import org.restlet.data.Tag;

import com.noelios.restlet.Factory;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Call wrapper for server HTTP calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServerRestletCall extends Call
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(HttpServerRestletCall.class.getCanonicalName());

   /** The HTTP server connector that issued the call. */
   protected Connector httpServer;

   /** The low-level connector call. */
   protected ConnectorCall connectorCall;

   /**
    * Constructor.
    * @param httpServer The HTTP server connector that issued the call.
    * @param call The wrapped HTTP server call.
    */
   public HttpServerRestletCall(Connector httpServer, AbstractHttpServerCall call)
   {
   	this.httpServer = httpServer;
   	
      // Set the properties
      setConnectorCall(call);
      getServer().setAddress(call.getResponseAddress());
      getServer().setName(Factory.VERSION_HEADER);
      setStatus(Statuses.SUCCESS_OK);
      setMethod(Methods.create(call.getRequestMethod()));

      // Set the resource reference
      String resource = call.getRequestUri();
      if(resource != null)
      {
         setResourceRef(resource);
      }
   }

   /**
    * Returns the condition data applying to this call.
    * @return The condition data applying to this call.
    */
   public ConditionData getCondition()
   {
      if(this.condition == null) 
      {
         this.condition = new ConditionData();

         // Extract the header values
         String ifMatchHeader = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_IF_MATCH);
         String ifNoneMatchHeader = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_IF_NONE_MATCH);
         Date ifModifiedSince = null;
         Date ifUnmodifiedSince = null;
         
         for(Parameter header : getConnectorCall().getRequestHeaders())
         {
            if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_IF_MODIFIED_SINCE))
            {
               ifModifiedSince = getConnectorCall().parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_IF_UNMODIFIED_SINCE))
            {
               ifUnmodifiedSince = getConnectorCall().parseDate(header.getValue(), false);
            }
         }
         
         // Set the If-Modified-Since date
         if((ifModifiedSince != null) && (ifModifiedSince.getTime() != -1))
         {
            getCondition().setModifiedSince(ifModifiedSince);
         }

         // Set the If-Unmodified-Since date
         if((ifUnmodifiedSince != null) && (ifUnmodifiedSince.getTime() != -1))
         {
            getCondition().setUnmodifiedSince(ifUnmodifiedSince);
         }

         // Set the If-Match tags
         List<Tag> match = null;
         Tag current = null;
         if(ifMatchHeader != null)
         {
            try
            {
               String[] tags = ifMatchHeader.split(",");
               for (int i = 0; i < tags.length; i++)
               {
                  current = new Tag(tags[i]);
               
                  // Is it the first tag?
                  if(match == null) 
                  {
                     match = new ArrayList<Tag>();
                     getCondition().setMatch(match);
                  }
                  
                  // Add the new tag
                  match.add(current);
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "Unable to process the if-match header: " + ifMatchHeader);
            }
         }

         // Set the If-None-Match tags
         List<Tag> noneMatch = null;
         if(ifNoneMatchHeader != null)
         {
            try
            {
               String[] tags = ifNoneMatchHeader.split(",");
               for (int i = 0; i < tags.length; i++)
               {
                  current = new Tag(tags[i]);
                  
                  // Is it the first tag?
                  if(noneMatch == null) 
                  {
                     noneMatch = new ArrayList<Tag>();
                     getCondition().setNoneMatch(noneMatch);
                  }
                  
                  noneMatch.add(current);
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "Unable to process the if-none-match header: " + ifNoneMatchHeader);
            }
         }
      }

      return this.condition;
   }

   /**
    * Returns the low-level connector call.
    * @return The low-level connector call.
    */
   public ConnectorCall getConnectorCall()
   {
   	return this.connectorCall;
   }

   /**
    * Returns the cookies provided by the client.
    * @return The cookies provided by the client.
    */
   public List<Cookie> getCookies()
   {
      if(this.cookies == null) 
      {
         this.cookies = new ArrayList<Cookie>();
         String cookiesValue = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_COOKIE);

         if(cookiesValue != null)
         {
	   		try
	         {
	            CookieReader cr = new CookieReader(cookiesValue);
	            Cookie current = cr.readCookie();
	            while(current != null)
	            {
	               this.cookies.add(current);
	               current = cr.readCookie();
	            }
	         }
	         catch(Exception e)
	         {
	            logger.log(Level.WARNING, "An exception occured during cookies parsing. Headers value: " + cookiesValue, e);
	         }
         }
      }
      
      return this.cookies;
   }

   /**
    * Returns the representation provided by the client.
    * @return The representation provided by the client.
    */
   public Representation getInput()
   {
   	if(this.input == null)
   	{
   		this.input = ((AbstractHttpServerCall)getConnectorCall()).getRequestInput();
   	}
      
      return this.input;
   }

   /**
    * Returns the client specific data.
    * @return The client specific data.
    */
   public ClientData getClient()
   {
      if(this.client == null) 
      {
         this.client = new ClientData();

         // Extract the header values
         String acceptCharset = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_ACCEPT_CHARSET);
         String acceptEncoding = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_ACCEPT_ENCODING);
         String acceptLanguage = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_ACCEPT_LANGUAGE);
         String acceptMediaType = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_ACCEPT);

         // Parse the headers and update the call preferences
         PreferenceUtils.parseCharacterSets(acceptCharset, this.client);
         PreferenceUtils.parseEncodings(acceptEncoding, this.client);
         PreferenceUtils.parseLanguages(acceptLanguage, this.client);
         PreferenceUtils.parseMediaTypes(acceptMediaType, this.client);

         // Set other properties
         this.client.setName(getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_USER_AGENT));
         this.client.setAddress(getConnectorCall().getRequestAddress());

         // Special handling for the non standard but common "X-Forwarded-For" header. 
   		boolean useForwardedForHeader = Boolean.parseBoolean(this.httpServer.getParameters().getFirstValue("useForwardedForHeader", false)); 
   		if(useForwardedForHeader)
   		{
		      // Lookup the "X-Forwarded-For" header
		      String header = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_X_FORWARDED_FOR);
		      if(header != null)
		      {
		      	String[] addresses = header.split(",");
	      		for(int i = addresses.length - 1; i >= 0; i--)
	      		{
	      			this.client.getAddresses().add(addresses[i].trim());
	      		}
		      }
   		}
      }
      
      return this.client;
   }

   /**
    * Returns the referrer reference if available.
    * @return The referrer reference.
    */
   public Reference getReferrerRef()
   {
      if(this.referrerRef == null)
      {
      	String referrerValue = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_REFERRER);
      	if(referrerValue != null)
      	{
      		this.referrerRef = new Reference(referrerValue);
      	}
      }
      
      return this.referrerRef;
   }

   /**
    * Returns the security data related to this call.
    * @return The security data related to this call.
    */
   public SecurityData getSecurity()
   {
      if(this.security == null) 
      {
         this.security = new SecurityData();

         if(getConnectorCall().isConfidential()) 
         {
            getSecurity().setConfidential(true);
         }
         else
         {
            // We don't want to autocreate the security data just for this information
            // Because that will by the default value of this property if read by someone.
         }

         // Extract the header value
         String authorization = getConnectorCall().getRequestHeaders().getValues(ConnectorCall.HEADER_AUTHORIZATION);

         // Set the challenge response
         getSecurity().setChallengeResponse(SecurityUtils.parseResponse(authorization));
      }

      return this.security;
   }

   /**
    * Sets the low-level connector call.
    * @param call The low-level connector call.
    */
   public void setConnectorCall(ConnectorCall call)
   {
      this.connectorCall = call;
   }

}
