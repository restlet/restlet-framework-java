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

import org.restlet.RestletException;
import org.restlet.data.CookieSetting;

/**
 * Default cookie setting implementation.
 */
public class CookieSettingImpl extends CookieImpl implements CookieSetting
{
   /** The user's comment. */
   protected String comment;

   /**
    * The maximum age in seconds. Use 0 to discard an existing cookie.
    */
   protected int maxAge;

   /** Indicates if cookie should only be transmitted by secure means. */
   protected boolean secure;

   /**
    * Default constructor.
    * @throws RestletException
    */
   public CookieSettingImpl() throws RestletException
   {
      this(0, null, null, null, null);
   }

   /**
    * Preferred constructor.
    * @param name The cookie's name.
    * @param value The cookie's value.
    */
   public CookieSettingImpl(String name, String value)
   {
      this(0, name, value, null, null);
   }

   /**
    * Preferred constructor.
    * @param version The cookie's version.
    * @param name The cookie's name.
    * @param value The cookie's value.
    */
   public CookieSettingImpl(int version, String name, String value)
   {
      this(version, name, value, null, null);
   }

   /**
    * Preferred constructor.
    * @param version The cookie's version.
    * @param name The cookie's name.
    * @param value The cookie's value.
    * @param path The cookie's path.
    * @param domain The cookie's domain name.
    */
   public CookieSettingImpl(int version, String name, String value, String path, String domain)
   {
      super(version, name, value, path, domain);
      this.comment = null;
      this.maxAge = -1;
      this.secure = false;
   }

   /**
    * Returns the comment for the user.
    * @return The comment for the user.
    */
   public String getComment()
   {
      return this.comment;
   }

   /**
    * Sets the comment for the user.
    * @param comment The comment for the user.
    */
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   /**
    * Returns the maximum age in seconds. Use 0 to discard an existing cookie.
    * @return The maximum age in seconds.
    */
   public int getMaxAge()
   {
      return this.maxAge;
   }

   /**
    * Sets the maximum age in seconds. Use 0 to discard an existing cookie.
    * @param maxAge The maximum age in seconds.
    */
   public void setMaxAge(int maxAge)
   {
      this.maxAge = maxAge;
   }

   /**
    * Indicates if cookie should only be transmitted by secure means.
    * @return True if cookie should only be transmitted by secure means.
    */
   public boolean isSecure()
   {
      return this.secure;
   }

   /**
    * Indicates if cookie should only be transmitted by secure means.
    * @param secure True if cookie should only be transmitted by secure means.
    */
   public void setSecure(boolean secure)
   {
      this.secure = secure;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Cookie setting";
   }

}
