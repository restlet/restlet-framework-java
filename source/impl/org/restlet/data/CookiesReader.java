/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.io.IOException;
import java.util.Map;

import org.restlet.RestletException;

/**
 * Cookies reader.
 */
public interface CookiesReader
{
   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws RestletException;

   /**
    * Reads the next cookie available or null.
    * @return The next cookie available or null.
    */
   public Cookie readCookie() throws RestletException;

   /** Closes the reader. */
   public void close() throws IOException;
}




