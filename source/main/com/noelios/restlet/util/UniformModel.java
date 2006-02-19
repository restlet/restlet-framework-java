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

package com.noelios.restlet.util;

import java.io.IOException;

import org.restlet.UniformCall;
import org.restlet.data.Cookie;
import org.restlet.data.Parameter;;

/**
 * Readable model wrapping an uniform call.<br/>
 * Can be passed directly passed to a string template.
 */
public class UniformModel implements ReadableModel
{
   public static final String NAME_CLIENT_ADDRESS = "clientAddress";
   public static final String NAME_CLIENT_NAME = "clientName";
   public static final String NAME_COOKIE = "cookie[\"";
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
   public static final String NAME_RESOURCE_QUERY_PARAM = "query[\"";
   public static final String NAME_RESOURCE_SCHEME = "scheme";
   public static final String NAME_RESOURCE_URI = "uri";
   public static final String NAME_RESOURCE_USER_INFO = "userInfo";
   public static final String NAME_SERVER_ADDRESS = "serverAddress";
   public static final String NAME_SERVER_NAME = "serverName";
   public static final String NAME_STATUS = "status";

   /** The wrapped call. */
   protected UniformCall call;

   /** The default value to return if a lookup fails or returns null. */
   protected String defaultValue;

   /**
    * Constructor.
    * @param call The wrapped uniform call.
    * @param defaultValue The default value to return if a lookup fails or returns null.
    */
   public UniformModel(UniformCall call, String defaultValue)
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

      if(name.equals(NAME_CLIENT_ADDRESS))
      {
         result = call.getClientAddress();
      }
      else if(name.equals(NAME_CLIENT_NAME))
      {
         result = call.getClientName();
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
      else if(name.equals(NAME_RESOURCE_QUERY))
      {
         result = call.getResourceRef().getQuery();
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
      else if(name.startsWith(NAME_RESOURCE_QUERY_PARAM))
      {
         int beginIndex = NAME_RESOURCE_QUERY_PARAM.length();
         int endIndex = name.indexOf("\"]");

         if(endIndex != -1)
         {
            try
            {
               String paramName = name.substring(beginIndex, endIndex);
               Parameter param = call.getResourceRef().getQueryAsForm().getFirstParameter(paramName);
               
               if(param != null)
                  result = param.getValue();
               else
                  result = null;
            }
            catch(IOException e)
            {
               result = null;
            }
         }
      }
      else if(name.startsWith(NAME_COOKIE))
      {
         int beginIndex = NAME_COOKIE.length();
         int endIndex = name.indexOf("\"]");

         if(endIndex != -1)
         {
            String cookieName = name.substring(beginIndex, endIndex);
            Cookie cookie = CookieUtils.getFirstCookie(call.getCookies(), cookieName);
            if(cookie != null) result = cookie.getValue();
         }
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
         result = Integer.toString(call.getStatus().getHttpCode());
      }

      // Check if the default value should be returned
      if(result == null)
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

      if(name.equals(NAME_CLIENT_ADDRESS))
      {
         result = (call.getClientAddress() != null);
      }
      else if(name.equals(NAME_CLIENT_NAME))
      {
         result = (call.getClientName() != null);
      }
      else if(name.startsWith(NAME_COOKIE))
      {
         result = (call.getCookies() != null);
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
      else if(name.equals(NAME_RESOURCE_QUERY))
      {
         result = (call.getResourceRef() != null) && (call.getResourceRef().getQuery() != null);
      }
      else if(name.startsWith(NAME_RESOURCE_QUERY_PARAM))
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
