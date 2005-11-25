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
import java.util.Map;

/**
 * Cookies reader.
 */
public interface CookiesReader
{
   /**
    * Reads the cookies whose name is a key in the given map. If a matching cookie is found, its value is put
    * in the map.
    * @param cookies The cookies map controlling the reading.
    * @throws IOException
    */
   public void readCookies(Map<String, Cookie> cookies) throws IOException;

   /**
    * Reads the next cookie available or null.
    * @return The next cookie available or null.
    * @throws IOException
    */
   public Cookie readCookie() throws IOException;

   /**
    * Closes the reader.
    * @throws IOException
    */
   public void close() throws IOException;
}
