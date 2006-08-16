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

import java.util.Date;

import org.restlet.data.ParameterList;

import com.noelios.restlet.util.DateUtils;

/**
 * Default implementation of a HTTP call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultHttpCall implements HttpCall
{
   /** Indicates if the call is confidential. */
	private boolean confidential;
   
   /** The client IP address. */
	private String requestAddress;
   
   /** The request method. */
	private String requestMethod;
   
   /** The request URI. */
	private String requestUri;
   
   /** The request headers. */
	private ParameterList requestHeaders;
   
   /** The response address. */
	private String responseAddress;
   
   /** The response headers. */
	private ParameterList responseHeaders;
   
   /** The response status code. */
	private int responseStatusCode;
   
   /** The response reason phrase. */
	private String responseReasonPhrase;
   
   /**
    * Constructor.
    */
   public DefaultHttpCall()
   {
      this.confidential = false;
      this.responseAddress = null;
      this.requestMethod = null;
      this.requestUri = null;
      this.requestHeaders = null;
      this.responseAddress = null;
      this.responseHeaders = null;
      this.responseStatusCode = 200;
      this.responseReasonPhrase = "";
   }

   /**
    * Indicates if the confidentiality of the call is ensured (ex: via SSL).
    * @return True if the confidentiality of the call is ensured (ex: via SSL).
    */
   public boolean isConfidential()
   {
      return this.confidential;
   }

   /**
    * Indicates if the confidentiality of the call is ensured (ex: via SSL).
    * @param confidential True if the confidentiality of the call is ensured (ex: via SSL).
    */
   protected void setConfidential(boolean confidential)
   {
      this.confidential = confidential;
   }

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
   public String getRequestAddress()
   {
      return this.requestAddress;
   }

   /**
    * Sets the request address. 
    * @param requestAddress The request address. 
    */
   protected void setRequestAddress(String requestAddress)
   {
      this.requestAddress = requestAddress;
   }

   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getRequestMethod()
   {
      return this.requestMethod;
   }
	
   /**
    * Sets the request method. 
    * @param method The request method.
    */
   protected void setRequestMethod(String method)
   {
      this.requestMethod = method;
   }

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri()
   {
      return this.requestUri;
   }

   /**
    * Sets the full request URI. 
    * @param requestUri The full request URI.
    */
   protected void setRequestUri(String requestUri)
   {
      this.requestUri = requestUri;
   }
   
   /**
    * Returns the modifiable list of request headers.
    * @return The modifiable list of request headers.
    */
   public ParameterList getRequestHeaders()
   {
      if(this.requestHeaders == null) this.requestHeaders = new ParameterList();
      return this.requestHeaders;
   }

   /**
    * Returns the response address.<br/>
    * Corresponds to the IP address of the responding server.
    * @return The response address.
    */
   public String getResponseAddress()
   {
      return this.responseAddress;
   }

   /**
    * Sets the response address.<br/>
    * Corresponds to the IP address of the responding server.
    * @param responseAddress The response address.
    */
   public void setResponseAddress(String responseAddress)
   {
      this.responseAddress = responseAddress;
   }
   
   /**
    * Returns the modifiable list of response headers.
    * @return The modifiable list of response headers.
    */
   public ParameterList getResponseHeaders()
   {
      if(this.responseHeaders == null) this.responseHeaders = new ParameterList();
      return this.responseHeaders;
   }

   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      return this.responseStatusCode;
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
      return this.responseReasonPhrase;
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
