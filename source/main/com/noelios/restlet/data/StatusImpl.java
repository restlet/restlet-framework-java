/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

package com.noelios.restlet.data;

import org.restlet.data.Status;
import org.restlet.data.Statuses;

/**
 * Default status implementation.
 * @see org.restlet.data.Statuses
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
    * @param code The specification code.
    * @param description The description of this REST element.
    * @param uri The URI of the specification describing the method.
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
      return (description == null) ? Statuses.getDescription(getHttpCode()) : description;
   }

   /**
    * Returns the URI of the specification describing the status.
    * @return The URI of the specification describing the status.
    */
   public String getUri()
   {
      return (uri == null) ? Statuses.getUri(getHttpCode()) : uri;
   }

   /**
    * Indicates if the status is an information status.
    * @return True if the status is an information status.
    */
   public boolean isInfo()
   {
      return Statuses.isInfo(getHttpCode());
   }

   /**
    * Indicates if the status is a success status.
    * @return True if the status is a success status.
    */
   public boolean isSuccess()
   {
      return Statuses.isSuccess(getHttpCode());
   }

   /**
    * Indicates if the status is a redirection status.
    * @return True if the status is a redirection status.
    */
   public boolean isRedirection()
   {
      return Statuses.isRedirection(getHttpCode());
   }

   /**
    * Indicates if the status is a client error status.
    * @return True if the status is a client error status.
    */
   public boolean isClientError()
   {
      return Statuses.isClientError(getHttpCode());
   }

   /**
    * Indicates if the status is a server error status.
    * @return True if the status is a server error status.
    */
   public boolean isServerError()
   {
      return Statuses.isServerError(getHttpCode());
   }

   /**
    * Indicates if the status is an error (client or server) status.
    * @return True if the status is an error (client or server) status.
    */
   public boolean isError()
   {
      return Statuses.isError(getHttpCode());
   }

   /**
    * Indicates if the method is equal to a given one.
    * @param status The status to compare to.
    * @return True if the status is equal to a given one.
    */
   public boolean equals(Status status)
   {
      return getHttpCode() == status.getHttpCode();
   }

}
