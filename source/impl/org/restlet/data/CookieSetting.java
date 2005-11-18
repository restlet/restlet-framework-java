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

/**
 * Cookie to set in a user agent.
 */
public interface CookieSetting extends Cookie
{
   /**
    * Returns the comment for the user.
    * @return The comment for the user.
    */
   public String getComment();

   /**
    * Sets the comment for the user.
    * @param comment The comment for the user.
    */
   public void setComment(String comment);

   /**
    * Returns the maximum age in seconds.
    * Use 0 to discard an existing cookie.
    * @return The maximum age in seconds.
    */
   public int getMaxAge();

   /**
    * Sets the maximum age in seconds.
    * Use 0 to discard an existing cookie.
    * @param maxAge The maximum age in seconds.
    */
   public void setMaxAge(int maxAge);

   /**
    * Indicates if cookie should only be transmitted by secure means.
    * @return True if cookie should only be transmitted by secure means.
    */
   public boolean isSecure();

   /**
    * Indicates if cookie should only be transmitted by secure means.
    * @param secure True if cookie should only be transmitted by secure means.
    */
   public void setSecure(boolean secure);

}




