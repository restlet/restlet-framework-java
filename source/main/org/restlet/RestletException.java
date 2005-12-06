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
    * @param cause The nested error or exception that caused this new one.
    */
   public RestletException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param cause The nested error or exception that caused this new one.
    */
   public RestletException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param suggestion A suggestion on what to do with this exception.
    */
   public RestletException(String message, String suggestion)
   {
      this(message, null, suggestion);
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param cause The nested error or exception that caused this new one.
    * @param suggestion A suggestion on what to do with this exception.
    */
   public RestletException(String message, Throwable cause, String suggestion)
   {
      this(message, cause, null, suggestion);
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param source The object at the source of the exception.
    * @param suggestion A suggestion on what to do with this exception.
    */
   public RestletException(String message, Object source, String suggestion)
   {
      super(message);
      this.source = source;
      this.suggestion = suggestion;
   }

   /**
    * Constructor.
    * @param message The error message.
    * @param cause The nested error or exception that caused this new one.
    * @param source The object at the source of the exception.
    * @param suggestion A suggestion on what to do with this exception.
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
