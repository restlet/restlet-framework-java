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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.data.Cookie;
import org.restlet.data.Cookies;
import org.restlet.data.CookiesReader;
import org.restlet.data.MediaTypes;

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
      super(new ByteArrayInputStream(cookiesHeader.getBytes()), MediaTypes.APPLICATION_WWW_FORM);
   }

   /**
    * Constructor.
    * @param cookiesInputStream The cookies stream to parse.
    */
   public CookiesImpl(InputStream cookiesInputStream) throws IOException
   {
      super(cookiesInputStream, MediaTypes.APPLICATION_WWW_FORM);
   }

   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws IOException
   {
      getCookiesReader().readCookies(cookies);
   }

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    */
   public CookiesReader getCookiesReader() throws IOException
   {
      return new CookiesReaderImpl(getStream());
   }

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    */
   public List<Cookie> getCookies() throws IOException
   {
      List<Cookie> result = new ArrayList<Cookie>();
      CookiesReader cis = getCookiesReader();
      Cookie cookie = cis.readCookie();

      while (cookie != null)
      {
         result.add(cookie);
         cis.readCookie();
      }

      cis.close();
      return result;
   }

}




