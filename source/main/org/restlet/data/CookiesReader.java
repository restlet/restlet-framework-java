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
import java.util.Map;

/**
 * Cookies reader.
 */
public interface CookiesReader
{
   /**
    * Reads the cookies whose name is a key in the given map.<br/>
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    * @throws IOException
    */
   public void readCookies(Map<String, Cookie> cookies) throws IOException;

   /**
    * Reads the first cookie available with the given name or null.
    * @return The first cookie available or null.
    * @throws IOException
    */
   public Cookie readFirstCookie(String name) throws IOException;

   /**
    * Reads the next cookie available or null.
    * @return The next cookie available or null.
    * @throws IOException
    */
   public Cookie readNextCookie() throws IOException;

   /**
    * Closes the reader.
    * @throws IOException
    */
   public void close() throws IOException;
}
