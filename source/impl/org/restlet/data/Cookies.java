/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

import java.util.List;
import java.util.Map;

import org.restlet.RestletException;

/**
 * List of cookies.
 */
public interface Cookies
{
   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws RestletException;

   /**
    * Returns a new cookies reader to read the list.
    * @return A new cookies reader to read the list.
    */
   public CookiesReader getCookiesReader() throws RestletException;

   /**
    * Returns the list of cookies.
    * @return The list of cookies.
    */
   public List<Cookie> getCookies() throws RestletException;

}




