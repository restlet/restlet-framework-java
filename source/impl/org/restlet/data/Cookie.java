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
 * Cookie received from a user agent.
 */
public interface Cookie extends Parameter
{
   /**
    * Returns the cookie specification version.
    * @return The cookie specification version.
    */
   public int getVersion();

   /**
    * Sets the cookie specification version.
    * @param version The cookie specification version.
    */
   public void setVersion(int version);

   /**
    * Returns the validity path.
    * @return The validity path.
    */
   public String getPath();

   /**
    * Sets the validity path.
    * @param path The validity path.
    */
   public void setPath(String path);

   /**
    * Returns the domain name.
    * @return The domain name.
    */
   public String getDomain();

   /**
    * Sets the domain name.
    * @param domain The domain name.
    */
   public void setDomain(String domain);

}




