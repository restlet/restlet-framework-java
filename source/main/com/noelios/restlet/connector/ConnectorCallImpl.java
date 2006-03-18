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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.Manager;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.Parameter;

import com.noelios.restlet.util.DateUtils;

/**
 * Implementation of a connector call.
 */
public class ConnectorCallImpl implements ConnectorCall
{
   /** Indicates if the call is confidential. */
   protected boolean confidential;
   
   /** The client IP address. */
   protected String requestAddress;
   
   /** The request method. */
   protected String requestMethod;
   
   /** The request URI. */
   protected String requestUri;
   
   /** The request headers. */
   protected List<Parameter> requestHeaders;
   
   /** The response address. */
   protected String responseAddress;
   
   /** The response headers. */
   protected List<Parameter> responseHeaders;
   
   /** The response status code. */
   protected int responseStatusCode;
   
   /** The response reason phrase. */
   protected String responseReasonPhrase;
   
   /**
    * Constructor.
    */
   public ConnectorCallImpl()
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
    * Adds a request header.
    * @param name The header's name.
    * @param value The header's value.
    */
   public void addRequestHeader(String name, String value)
   {
      getRequestHeaders().add(Manager.createParameter(name, value));
   }

   /**
    * Adds a response header.
    * @param name The header's name.
    * @param value The header's value.
    */
   public void addResponseHeader(String name, String value)
   {
      getResponseHeaders().add(Manager.createParameter(name, value));
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
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
   public String getRequestAddress()
   {
      return this.requestAddress;
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
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri()
   {
      return this.requestUri;
   }
   
   /**
    * Returns the modifiable list of request headers.
    * @return The modifiable list of request headers.
    */
   public List<Parameter> getRequestHeaders()
   {
      if(this.requestHeaders == null) this.requestHeaders = new ArrayList<Parameter>();
      return this.requestHeaders;
   }
   
   /**
    * Returns the value for a request header name.<br/>
    * If multiple headers with the same name are found, all values are returned separated by commas.
    * @param headerName The header name.
    * @return The value for a request header name.
    */
   public String getRequestHeaderValue(String headerName)
   {
   	return getHeaderValue(headerName, getRequestHeaders());
   }

   /**
    * Returns the value for a header name.<br/>
    * If multiple headers with the same name are found, all values are returned separated by commas.
    * @param headerName The header name.
    * @param headers The headers list.
    * @return The value for a header name.
    */
   private String getHeaderValue(String headerName, List<Parameter> headers)
   {
   	String result = null;
   	StringBuilder sb = null;
   	
   	for(Parameter header : getRequestHeaders())
   	{
   		if(header.getName().equalsIgnoreCase(headerName))
   		{
   			if(sb == null)
   			{
   				if(result == null)
   				{
   					result = header.getValue();
   				}
   				else
   				{
   					sb = new StringBuilder();
      				sb.append(result).append(',').append(header.getValue());
   				}
   			}
   			else
   			{
   				sb.append(',').append(header.getValue());
   			}
   		}
   	}
   	
   	if(sb != null)
   	{
   		result = sb.toString();
   	}
   	
   	return result;
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
    * Returns the modifiable list of response headers.
    * @return The modifiable list of response headers.
    */
   public List<Parameter> getResponseHeaders()
   {
      if(this.responseHeaders == null) this.responseHeaders = new ArrayList<Parameter>();
      return this.responseHeaders;
   }
   
   /**
    * Returns the value for a response header name.<br/>
    * If multiple headers with the same name are found, all values are returned separated by commas.
    * @param headerName The header name.
    * @return The value for a response header name.
    */
   public String getResponseHeaderValue(String headerName)
   {
   	return getHeaderValue(headerName, getResponseHeaders());
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
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1036);
      }
      else
      {
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1123);
      }
   }

}
