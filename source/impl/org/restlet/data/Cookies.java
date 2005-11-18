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

package org.restlet.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents the cookies sent by a user agent.
 */
public interface Cookies
{
   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws IOException;

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    */
   public CookiesReader getCookiesReader() throws IOException;

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    */
   public List<Cookie> getCookies() throws IOException;

}




