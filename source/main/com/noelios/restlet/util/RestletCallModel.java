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

package com.noelios.restlet.util;

import org.restlet.RestletCall;

/**
 * Wraps a restlet call into a readable model
 * that can be passed to the string template.
 */
public class RestletCallModel implements ReadableModel
{
   public static final String NAME_CLIENT_ADDRESS = "clientAddress";
   public static final String NAME_CLIENT_NAME = "clientName";
   public static final String NAME_METHOD = "method";
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
   
   /** The wrapped restlet call. */
   protected RestletCall call;
   
   /**
    * Constructor.
    * @param call The wrapped restlet call.
    */
   public RestletCallModel(RestletCall call)
   {
      this.call = call;
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
      
/*      
      // Put cookies
      if(call.getCookies() != null)
      {
         List<Cookie> cookies;
         try   
         {
            cookies = call.getCookies().getCookies();
            for(int i = 0; i < cookies.size(); i++)
            {
               data.put("cookie[" + i + "]", call.getClientName());
            }
         }
         catch(IOException e)
         {
            logger.log(Level.WARNING, "Unable to read the cookies", e);
         }
      }
      
      // Put query params
      if(call.getResourceUri().getQuery() != null)
      {
         List<Parameter> params;
         try   
         {
            params = call.getResourceUri().getQueryAsForm().getParameters();
            for(int i = 0; i < params.size(); i++)
            {
               data.put("resourceUri.query.name[" + i + "]", params.get(i).getName());
               data.put("resourceUri.query.value[" + i + "]", params.get(i).getValue());
            }
         }
         catch(IOException e)
         {
            logger.log(Level.WARNING, "Unable to read the cookies", e);
         }
      }
*/
      
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
      else if(name.equals(NAME_METHOD))
      {
         result = (call.getMethod() != null);
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
      
      return result;
   }
   
}
