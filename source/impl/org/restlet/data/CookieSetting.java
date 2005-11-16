/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
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




