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
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;

/**
 * Cookie manipulation utilities.
 */
public class CookieUtils
{
   /**
    * Formats a list of cookies as an HTTP header.
    * @param cookies The list of cookies to format.
    * @return The HTTP header.
    */
   public static String format(List<Cookie> cookies)
   {
      StringBuilder sb = new StringBuilder();
      
      Cookie cookie;
      for(int i = 0; i < cookies.size(); i++)
      {
         cookie = cookies.get(i);

         if(i == 0)
         {
            if(cookie.getVersion() > 0)
            {
               sb.append("$Version=\"").append(cookie.getVersion()).append("\"; ");
            }
         }
         else
         {
            sb.append("; ");
         }
         
         format(cookie, sb);
      }
      
      return sb.toString();
   }
   
   /**
    * Formats a cookie setting.
    * @param cookieSetting The cookie setting to format.
    * @return The formatted cookie setting.
    */
   public static String format(CookieSetting cookieSetting)
   {
      StringBuilder sb = new StringBuilder();
      
      try
      {
         format(cookieSetting, sb);
      }
      catch(IOException e)
      {
         // log error
      }
      
      return sb.toString();
   }
   
   /**
    * Formats a cookie setting.
    * @param cookieSetting The cookie setting to format.
    * @param destination The appendable destination.
    */
   public static void format(CookieSetting cookieSetting, Appendable destination) throws IOException
   {
      String name = cookieSetting.getName();
      String value = cookieSetting.getValue();
      int version = cookieSetting.getVersion();
      
      if((name == null) || (name.length() == 0))
      {
         throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
      }
      else
      {
         destination.append(name).append('=');
         
         // Append the value
         if((value != null) && (value.length() > 0))
         {
            appendValue(value, version, destination);
         }

         // Append the version 
         if(version > 0)
         {
            destination.append("; Version=");
            appendValue(Integer.toString(version), version, destination);
         }
         
         // Append the path
         String path = cookieSetting.getPath();
         if((path != null) && (path.length() > 0))
         {
            destination.append("; Path=");

            if(version == 0)
            {
               destination.append(path);
            }
            else
            {
               HeaderUtils.appendQuote(path, destination);
            }
         }

         // Append the expiration date
         int maxAge = cookieSetting.getMaxAge();
         if(maxAge >= 0)
         {
            if(version == 0)
            {
               long currentTime = System.currentTimeMillis();
               long maxTime = ((long)maxAge * 1000L);
               long expiresTime = currentTime + maxTime;
               Date expires = new Date(expiresTime);
               destination.append("; Expires=");
               appendValue(DateUtils.format(expires, DateUtils.FORMAT_RFC_1036[0]), version, destination);
            }
            else
            {
               destination.append("; Max-Age=");
               appendValue(Integer.toString(cookieSetting.getMaxAge()), version, destination);
            }
         }
         else if((maxAge == -1) && (version > 0))
         {
            // Discard the cookie at the end of the user's session (RFC 2965)
            destination.append("; Discard");
         }
         else
         {
            // Netscape cookies automatically expire at the end of the user's session
         }

         // Append the domain
         String domain = cookieSetting.getDomain();
         if((domain != null) && (domain.length() > 0))
         {
            destination.append("; Domain=");
            appendValue(domain.toLowerCase(), version, destination);
         }

         // Append the secure flag
         if(cookieSetting.isSecure())
         {
            destination.append("; Secure");
         }
         
         // Append the comment
         if(version > 0)
         {
            String comment = cookieSetting.getComment();
            if((comment != null) && (comment.length() > 0))
            {
               destination.append("; Comment=");
               appendValue(comment, version, destination);
            }
         }
      }
   }
   
   /**
    * Formats a cookie.
    * @param cookie The cookie to format.
    * @return The formatted cookie.
    */
   public static String format(Cookie cookie)
   {
      StringBuilder sb = new StringBuilder();
      format(cookie, sb);
      return sb.toString();
   }
   
   /**
    * Formats a cookie setting.
    * @param cookie The cookie to format.
    * @param destination The appendable destination.
    */
   public static void format(Cookie cookie, Appendable destination)
   {
      String name = cookie.getName();
      String value = cookie.getValue();
      int version = cookie.getVersion();
      
      if((name == null) || (name.length() == 0))
      {
         throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
      }
      else
      {
         try
         {
            appendValue(name, 0, destination).append('=');
            
            // Append the value
            if((value != null) && (value.length() > 0))
            {
               appendValue(value, version, destination);
            }
            
            if(version > 0)
            {
               // Append the path
               String path = cookie.getPath();
               if((path != null) && (path.length() > 0))
               {
                  destination.append("; $Path=");
                  HeaderUtils.appendQuote(path, destination);
               }
               
               // Append the domain
               String domain = cookie.getDomain();
               if((domain != null) && (domain.length() > 0))
               {
                  destination.append("; $Domain=");
                  HeaderUtils.appendQuote(domain, destination);
               }
            }
         }
         catch(IOException e)
         {
            // log error
         }
      }
   }
   
   /**
    * Appends a source string as an HTTP comment.
    * @param value The source string to format.
    * @param version The cookie version. 
    * @param destination The appendable destination.
    * @throws IOException
    */
   private static Appendable appendValue(CharSequence value, int version, Appendable destination) throws IOException
   {
      if(version == 0)
      {
         destination.append(URLEncoder.encode(value.toString(), "UTF-8"));
      }
      else
      {
         HeaderUtils.appendQuote(value, destination);
      }
      
      return destination;
   }
   
   
   /**
    * Gets the cookies whose name is a key in the given map. 
    * If a matching cookie is found, its value is put in the map.
    * @param source The source list of cookies.
    * @param destination The cookies map controlling the reading.
    */
   public static void getCookies(List<Cookie> source, Map<String, Cookie> destination)
   {
      Cookie cookie;
      
      for(Iterator<Cookie> iter = source.iterator(); iter.hasNext(); )
      {
         cookie = iter.next();

         if(destination.containsKey(cookie.getName()))
         {
            destination.put(cookie.getName(), cookie);
         }
      }
   }

   /**
    * Reads the first cookie available with the given name or null.
    * @param source The source list of cookies.
    * @param name The name of the cookie to return.
    * @return The first cookie available with the given name or null.
    */
   public static Cookie getFirstCookie(List<Cookie> source, String name)
   {
      Cookie result = null;
      Cookie cookie;
      
      for(Iterator<Cookie> iter = source.iterator(); (result == null) && iter.hasNext(); )
      {
         cookie = iter.next();

         if(name.equals(cookie.getName()))
         {
            result = cookie;
         }
      }
      
      return result;
   }

}
