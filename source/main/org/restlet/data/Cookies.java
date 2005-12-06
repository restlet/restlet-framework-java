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
    * Reads the cookies whose name is a key in the given map. If a matching cookie is found, its value is put
    * in the map.
    * @param cookies The cookies map controlling the reading.
    * @throws IOException
    */
   public void readCookies(Map<String, Cookie> cookies) throws IOException;

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    * @throws IOException
    */
   public CookiesReader getCookiesReader() throws IOException;

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    * @throws IOException
    */
   public List<Cookie> getCookies() throws IOException;

}
