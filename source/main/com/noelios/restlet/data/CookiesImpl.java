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
    * @throws IOException
    */
   public CookiesImpl(InputStream cookiesInputStream) throws IOException
   {
      super(cookiesInputStream, MediaTypes.APPLICATION_WWW_FORM);
   }

   /**
    * Reads the cookies whose name is a key in the given map. If a matching cookie is found, its value is put
    * in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void getCookies(Map<String, Cookie> cookies) throws IOException
   {
      getCookiesReader().readCookies(cookies);
   }

   /**
    * Gets the first cookie available with the given name or null.
    * @return The first cookie available or null.
    * @throws IOException
    */
   public Cookie getFirstCookie(String name) throws IOException
   {
      return getCookiesReader().readFirstCookie(name);
   }

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    */
   public List<Cookie> getCookies() throws IOException
   {
      List<Cookie> result = new ArrayList<Cookie>();
      CookiesReader cis = getCookiesReader();
      Cookie cookie = cis.readNextCookie();

      while(cookie != null)
      {
         result.add(cookie);
         cis.readNextCookie();
      }

      cis.close();
      return result;
   }

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    */
   public CookiesReader getCookiesReader() throws IOException
   {
      return new CookiesReaderImpl(getStream());
   }

}
