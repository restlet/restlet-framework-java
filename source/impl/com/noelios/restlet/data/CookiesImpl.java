/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.RestletException;
import org.restlet.data.Cookie;
import org.restlet.data.Cookies;
import org.restlet.data.CookiesReader;
import org.restlet.data.MediaTypeEnum;

/**
 * Default cookies implementation.
 */
public class CookiesImpl extends InputRepresentation implements Cookies
{
   /**
    * Constructor.
    * @param cookiesHeader The cookies header to parse.
    */
   public CookiesImpl(String cookiesHeader)
   {
      super(new ByteArrayInputStream(cookiesHeader.getBytes()), MediaTypeEnum.APPLICATION_WWW_FORM);
   }

   /**
    * Constructor.
    * @param cookiesInputStream The cookies stream to parse.
    */
   public CookiesImpl(InputStream cookiesInputStream) throws RestletException
   {
      super(cookiesInputStream, MediaTypeEnum.APPLICATION_WWW_FORM);
   }

   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws RestletException
   {
      getCookiesReader().readCookies(cookies);
   }

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    */
   public CookiesReader getCookiesReader() throws RestletException
   {
      return new CookiesReaderImpl(getStream());
   }

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    */
   public List<Cookie> getCookies() throws RestletException
   {
      List<Cookie> result = new ArrayList<Cookie>();

      try
      {
         CookiesReader cis = getCookiesReader();
         Cookie cookie = cis.readCookie();
         while (cookie != null)
         {
            result.add(cookie);
            cis.readCookie();
         }
         cis.close();
      }
      catch (Exception e)
      {
         throw new RestletException("Error while reading the call's cookies", e);
      }

      return result;
   }

}




