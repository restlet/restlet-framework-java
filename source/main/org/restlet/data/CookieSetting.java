/*
 * Copyright 2005-2006 Jerome LOUVEL
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

/**
 * Cookie setting provided by a server.
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
    * Returns the maximum age in seconds.<br/>
    * Use 0 to immediately discard an existing cookie.<br/>
    * Use -1 to discard the cookie at the end of the session (default).
    * @return The maximum age in seconds.
    */
   public int getMaxAge();

   /**
    * Sets the maximum age in seconds.<br/> 
    * Use 0 to immediately discard an existing cookie.<br/>
    * Use -1 to discard the cookie at the end of the session (default).
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
