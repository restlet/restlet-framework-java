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

import org.restlet.data.Status;

/** 
 * Default status implementation. 
 * @see org.restlet.data.StatusEnum
 */
public class StatusImpl implements Status
{
   /** The specification code. */
   private int code;
   
   /** The description of this REST element. */
   private String description;
   
   /** The URI of the specification describing the method. */
   private String uri;
   
   /**
    * Constructor.
    * @param code The specification code.
    */
   public StatusImpl(int code)
   {
      this(code, null, null);
   }
   
   /**
    * Constructor.
    * @param code          The specification code.
    * @param description   The description of this REST element.
    * @param uri           The URI of the specification describing the method.
    */
   public StatusImpl(int code, String description, String uri)
   {
      this.code = code;
      this.description = description;
      this.uri = uri;
   }
   
   /**
    * Returns the HTTP code.
    * @return The HTTP code.
    */
   public int getHttpCode()
   {
      return code;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Returns the URI of the specification describing the status.
    * @return The URI of the specification describing the status.
    */
   public String getUri()
   {
      return uri;
   }

   /**
    * Indicates if the method is equal to a given one.
    * @param status  The status to compare to.
    * @return        True if the status is equal to a given one.
    */
   public boolean equals(Status status)
   {
      return getHttpCode() == status.getHttpCode();
   }

}
