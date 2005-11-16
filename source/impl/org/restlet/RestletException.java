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

package org.restlet;

/**
 * Generic exception used in the Restlet framework.
 */
public class RestletException extends Exception
{
   private static final long serialVersionUID = 1L;

   /** The object at the source of the exception. */
   private Object source;

   /** A suggestion on what to do with this exception. */
   private String suggestion;

   /** Constructor. */
   public RestletException()
   {
      super();
   }

   /**
    * Constructor.
    * @param message The error message.
    */
   public RestletException(String message)
   {
      super(message);
   }

   /**
    * Constructor.
    * @param cause	The nested error or exception that caused this new one.
    */
   public RestletException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param cause	The nested error or exception that caused this new one.
    */
   public RestletException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructor.
    * @param message 	The error message.
    * @param suggestion	A suggestion on what to do with this exception.
    */
   public RestletException(String message, String suggestion)
   {
      this(message, null, suggestion);
   }

   /**
    * Constructor.
    * @param message 	The error message.
    * @param cause		The nested error or exception that caused this new one.
    * @param suggestion	A suggestion on what to do with this exception.
    */
   public RestletException(String message, Throwable cause, String suggestion)
   {
      this(message, cause, null, suggestion);
   }

   /**
    * Constructor.
    * @param message 	The error message.
    * @param source		The object at the source of the exception.
    * @param suggestion	A suggestion on what to do with this exception.
    */
   public RestletException(String message, Object source, String suggestion)
   {
      super(message);
      this.source = source;
      this.suggestion = suggestion;
   }

   /**
    * Constructor.
    * @param message 	The error message.
    * @param cause		The nested error or exception that caused this new one.
    * @param source		The object at the source of the exception.
    * @param suggestion	A suggestion on what to do with this exception.
    */
   public RestletException(String message, Throwable cause, Object source, String suggestion)
   {
      super(message, cause);
      this.source = source;
      this.suggestion = suggestion;
   }

   /**
    * Returns a suggestion on what to do with this exception.
    * @return A suggestion on what to do with this exception.
    */
   public String getSuggestion()
   {
      return suggestion;
   }

   /**
    * Returns the object at the source of the exception.
    * @return The object at the source of the exception.
    */
   public Object getSource()
   {
      return source;
   }

}

