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

package com.noelios.restlet.util;

import java.util.List;

import org.restlet.Call;
import org.restlet.data.Parameter;

/**
 * Readable model wrapping a REST call. It can be passed directly passed to a string template. 
 * Repeating values can be retrieved by appending [index] or ["name"] after the variable's name. 
 * Note that [first] is equivalent to [0] and that [last] returns the last value. 
 * Here is the list of currently supported variables:
 * <ul>
 *	<li>clientAddress (repeating and non-repeating, lookup by index only)</li>
 * <li>clientName</li>
 * <li>cookie (repeating, lookup by name and by index)</li>
 * <li>method</li>
 * <li>redirectUri</li>
 * <li>referrerUri</li>
 * <li>authority</li>
 * <li>fragment</li>
 * <li>hostName</li>
 * <li>hostPort</li>
 * <li>hostIdentifier</li>
 * <li>identifier</li>
 * <li>path</li>
 * <li>query (repeating and non-repeating, lookup by name and by index)</li>
 * <li>requestHeader (repeating, lookup by name)</li>
 * <li>responseHeader (repeating, lookup by name)</li>
 * <li>scheme</li>
 * <li>uri</li>
 * <li>userInfo</li>
 * <li>serverAddress</li>
 * <li>serverName</li>
 * <li>status</li>
 * </ul>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class CallModel implements ReadableModel
{
   public static final String NAME_CLIENT_ADDRESS = "clientAddress";
   public static final String NAME_CLIENT_NAME = "clientName";
   public static final String NAME_CONNECTOR_REQUEST_HEADER = "requestHeader";
   public static final String NAME_CONNECTOR_RESPONSE_HEADER = "responseHeader";
   public static final String NAME_COOKIE = "cookie";
   public static final String NAME_METHOD = "method";
   public static final String NAME_REDIRECT_URI = "redirectUri";
   public static final String NAME_REFERRER_URI = "referrerUri";
   public static final String NAME_RESOURCE_AUTHORITY = "authority";
   public static final String NAME_RESOURCE_FRAGMENT = "fragment";
   public static final String NAME_RESOURCE_HOST_NAME = "hostName";
   public static final String NAME_RESOURCE_HOST_PORT = "hostPort";
   public static final String NAME_RESOURCE_HOST_IDENTIFIER = "hostIdentifier";
   public static final String NAME_RESOURCE_IDENTIFIER = "identifier";
   public static final String NAME_RESOURCE_PATH = "path";
   public static final String NAME_RESOURCE_QUERY = "query";
   public static final String NAME_RESOURCE_SCHEME = "scheme";
   public static final String NAME_RESOURCE_URI = "uri";
   public static final String NAME_RESOURCE_USER_INFO = "userInfo";
   public static final String NAME_SERVER_ADDRESS = "serverAddress";
   public static final String NAME_SERVER_NAME = "serverName";
   public static final String NAME_STATUS = "status";

   /** The wrapped call. */
   protected Call call;

   /** The default value to return if a lookup fails or returns null. */
   protected String defaultValue;

   /**
    * Constructor.
    * @param call The wrapped uniform call.
    * @param defaultValue The default value to return if a lookup fails or returns null.
    */
   public CallModel(Call call, String defaultValue)
   {
      this.call = call;
      this.defaultValue = defaultValue;
   }

   /**
    * Returns the model value for a given name.
    * @param name The name to look-up.
    * @return The model value for the given name.
    */
   public String get(String name)
   {
      String result = null;

      try
      {
	      if(name.startsWith(NAME_CLIENT_ADDRESS))
	      {
	      	if(name.equals(NAME_CLIENT_ADDRESS))
	      	{
	      		result = call.getClientAddress();
	      	}
	      	else
	      	{
	      		String rest = name.substring(NAME_CLIENT_ADDRESS.length());
	
	      		if((rest.charAt(0) == '[') && (rest.charAt(rest.length() - 1) == ']'))
	      		{
	      			rest = rest.substring(1, rest.length() - 1);
	      			
	   				if(rest.equals("first"))
	   				{
	   					result = call.getClientAddresses().get(0);
	   				}
	   				else if(rest.equals("last"))
	   				{
	   					result = call.getClientAddresses().get(call.getClientAddresses().size() - 1);
	   				}
	   				else if((rest.charAt(0) == '"') && (rest.charAt(rest.length() - 1) == '"'))
	   				{
	   					// Can't lookup by name
	   					result = defaultValue;
	   				}
	   				else
	   				{
	   					// Lookup by index
	   					result = call.getClientAddresses().get(Integer.parseInt(rest));
	   				}
	      		}
	      		else
	      		{
	      			result = defaultValue;
	      		}
	      	}
	      }
	      else if(name.equals(NAME_CLIENT_NAME))
	      {
	         result = call.getClientName();
	      }
	      else if(name.startsWith(NAME_CONNECTOR_REQUEST_HEADER))
	      {
	   		String rest = name.substring(NAME_CONNECTOR_REQUEST_HEADER.length());
	
	   		if((rest.charAt(0) == '[') && (rest.charAt(rest.length() - 1) == ']'))
	   		{
	   			rest = rest.substring(1, rest.length() - 1);
	   			
					if((rest.charAt(0) == '"') && (rest.charAt(rest.length() - 1) == '"'))
					{
						// Lookup by name
		   			rest = rest.substring(1, rest.length() - 1);
			         result = call.getConnectorCall().getRequestHeaderValue(rest);
					}
					else
					{
		   			result = defaultValue;
					}
	   		}
	   		else
	   		{
	   			result = defaultValue;
	   		}
	      }
	      else if(name.startsWith(NAME_CONNECTOR_RESPONSE_HEADER))
	      {
	   		String rest = name.substring(NAME_CONNECTOR_RESPONSE_HEADER.length());
	
	   		if((rest.charAt(0) == '[') && (rest.charAt(rest.length() - 1) == ']'))
	   		{
	   			rest = rest.substring(1, rest.length() - 1);
	   			
					if((rest.charAt(0) == '"') && (rest.charAt(rest.length() - 1) == '"'))
					{
						// Lookup by name
		   			rest = rest.substring(1, rest.length() - 1);
			         result = call.getConnectorCall().getResponseHeaderValue(rest);
					}
					else
					{
		   			result = defaultValue;
					}
	   		}
	   		else
	   		{
	   			result = defaultValue;
	   		}
	      }
	      else if(name.startsWith(NAME_COOKIE))
	      {
	   		String rest = name.substring(NAME_COOKIE.length());
	
	   		if((rest.charAt(0) == '[') && (rest.charAt(rest.length() - 1) == ']'))
	   		{
	   			rest = rest.substring(1, rest.length() - 1);
	   			
					if(rest.equals("first"))
					{
			         result = call.getCookies().get(0).getValue();
					}
					else if(rest.equals("last"))
					{
						result = call.getCookies().get(call.getCookies().size() - 1).getValue();
					}
					else if((rest.charAt(0) == '"') && (rest.charAt(rest.length() - 1) == '"'))
					{
						// Lookup by name
		   			rest = rest.substring(1, rest.length() - 1);
			         result = CookieUtils.getFirstCookie(call.getCookies(), rest).getValue();
					}
					else
					{
						// Lookup by index
						result = call.getCookies().get(Integer.parseInt(rest)).getValue();
					}
	   		}
	   		else
	   		{
	   			result = defaultValue;
	   		}
	      }
	      else if(name.equals(NAME_METHOD))
	      {
	         result = call.getMethod().getName();
	      }
	      else if(name.equals(NAME_REDIRECT_URI))
	      {
	         result = call.getRedirectionRef().toString();
	      }
	      else if(name.equals(NAME_REFERRER_URI))
	      {
	         result = call.getReferrerRef().toString();
	      }
	      else if(name.equals(NAME_RESOURCE_AUTHORITY))
	      {
	         result = call.getResourceRef().getAuthority();
	      }
	      else if(name.equals(NAME_RESOURCE_FRAGMENT))
	      {
	         result = call.getResourceRef().getFragment();
	      }
	      else if(name.equals(NAME_RESOURCE_HOST_NAME))
	      {
	         result = call.getResourceRef().getHostName();
	      }
	      else if(name.equals(NAME_RESOURCE_HOST_PORT))
	      {
	         result = call.getResourceRef().getHostPort().toString();
	      }
	      else if(name.equals(NAME_RESOURCE_HOST_IDENTIFIER))
	      {
	         result = call.getResourceRef().getHostIdentifier();
	      }
	      else if(name.equals(NAME_RESOURCE_IDENTIFIER))
	      {
	         result = call.getResourceRef().getIdentifier();
	      }
	      else if(name.equals(NAME_RESOURCE_PATH))
	      {
	         result = call.getResourceRef().getPath();
	      }
	      else if(name.startsWith(NAME_RESOURCE_QUERY))
	      {
	      	if(name.equals(NAME_RESOURCE_QUERY))
	      	{
		         result = call.getResourceRef().getQuery();
	      	}
	      	else
	      	{
	      		String rest = name.substring(NAME_RESOURCE_QUERY.length());
	
	      		if((rest.charAt(0) == '[') && (rest.charAt(rest.length() - 1) == ']'))
	      		{
	      			rest = rest.substring(1, rest.length() - 1);
	      			
	   				if(rest.equals("first"))
	   				{
	   					result = call.getResourceRef().getQueryAsForm().getParameters().get(0).getValue();
	   				}
	   				else if(rest.equals("last"))
	   				{
	   					List<Parameter> params = call.getResourceRef().getQueryAsForm().getParameters(); 
	   					result = params.get(params.size() - 1).getValue();
	   				}
	   				else if((rest.charAt(0) == '"') && (rest.charAt(rest.length() - 1) == '"'))
	   				{
							// Lookup by name
			   			rest = rest.substring(1, rest.length() - 1);
				         result = call.getResourceRef().getQueryAsForm().getFirstParameter(rest).getValue();
	   				}
	   				else
	   				{
	   					// Lookup by index
	   					result = call.getResourceRef().getQueryAsForm().getParameters().get(Integer.parseInt(rest)).getValue();
	   				}
	      		}
	      		else
	      		{
	      			result = defaultValue;
	      		}
	      	}
	      }
	      else if(name.equals(NAME_RESOURCE_SCHEME))
	      {
	         result = call.getResourceRef().getScheme();
	      }
	      else if(name.equals(NAME_RESOURCE_URI))
	      {
	         result = call.getResourceRef().toString();
	      }
	      else if(name.equals(NAME_RESOURCE_USER_INFO))
	      {
	         result = call.getResourceRef().getUserInfo();
	      }
	      else if(name.equals(NAME_SERVER_ADDRESS))
	      {
	         result = call.getServerAddress();
	      }
	      else if(name.equals(NAME_SERVER_NAME))
	      {
	         result = call.getServerName();
	      }
	      else if(name.equals(NAME_STATUS))
	      {
	         result = Integer.toString(call.getStatus().getCode());
	      }
	
	      // Check if the default value should be returned
	      if(result == null)
	      {
	         result = this.defaultValue;
	      }
      }
      catch(Exception e)
      {
         result = this.defaultValue;
      }
   
      return result;
   }

   /**
    * Indicates if the model contains a value for a given name.
    * @param name The name to look-up.
    * @return True if the model contains a value for the given name.
    */
   public boolean contains(String name)
   {
      boolean result = false;

      if(name.startsWith(NAME_CLIENT_ADDRESS))
      {
         result = (call.getClientAddress() != null);
      }
      else if(name.equals(NAME_CLIENT_NAME))
      {
         result = (call.getClientName() != null);
      }
      else if(name.startsWith(NAME_COOKIE))
      {
         result = (call.getCookies() != null) && (call.getCookies().size() > 0);
      }
      else if(name.equals(NAME_METHOD))
      {
         result = (call.getMethod() != null);
      }
      else if(name.equals(NAME_REDIRECT_URI))
      {
         result = (call.getRedirectionRef() != null);
      }
      else if(name.equals(NAME_REFERRER_URI))
      {
         result = (call.getReferrerRef() != null);
      }
      else if(name.equals(NAME_RESOURCE_AUTHORITY))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getAuthority() != null);
      }
      else if(name.equals(NAME_RESOURCE_FRAGMENT))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getFragment() != null);
      }
      else if(name.equals(NAME_RESOURCE_HOST_NAME))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getHostName() != null);
      }
      else if(name.equals(NAME_RESOURCE_HOST_PORT))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getHostPort() != null);
      }
      else if(name.equals(NAME_RESOURCE_HOST_IDENTIFIER))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getHostIdentifier() != null);
      }
      else if(name.equals(NAME_RESOURCE_IDENTIFIER))
      {
         result = (call.getResourceRef() != null);
      }
      else if(name.equals(NAME_RESOURCE_PATH))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getPath() != null);
      }
      else if(name.startsWith(NAME_RESOURCE_QUERY))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getQuery() != null);
      }
      else if(name.equals(NAME_RESOURCE_SCHEME))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getScheme() != null);
      }
      else if(name.equals(NAME_RESOURCE_URI))
      {
         result = (call.getResourceRef() != null);
      }
      else if(name.equals(NAME_RESOURCE_USER_INFO))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getUserInfo() != null);
      }
      else if(name.equals(NAME_SERVER_ADDRESS))
      {
         result = (call.getServerAddress() != null);
      }
      else if(name.equals(NAME_SERVER_NAME))
      {
         result = (call.getServerName() != null);
      }
      else if(name.equals(NAME_STATUS))
      {
         result = (call.getStatus() != null);
      }

      return result;
   }

}
