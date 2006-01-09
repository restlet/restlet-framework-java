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

package org.restlet.data;

/**
 * Status to return after handling a uniform call.
 * @see org.restlet.data.Statuses
 */
public interface Status extends ControlData
{
   /**
    * Returns the HTTP code.
    * @return The HTTP code.
    */
   public int getHttpCode();

   /**
    * Returns the URI of the specification describing the status.
    * @return The URI of the specification describing the status.
    */
   public String getUri();

   /**
    * Indicates if the status is equal to a given one.
    * @param status The status to compare to.
    * @return True if the status is equal to a given one.
    */
   public boolean equals(Status status);

   /**
    * Indicates if the status is an information status.
    * @return True if the status is an information status.
    */
   public boolean isInfo();

   /**
    * Indicates if the status is a success status.
    * @return True if the status is a success status.
    */
   public boolean isSuccess();

   /**
    * Indicates if the status is a redirection status.
    * @return True if the status is a redirection status.
    */
   public boolean isRedirection();

   /**
    * Indicates if the status is a client error status.
    * @return True if the status is a client error status.
    */
   public boolean isClientError();

   /**
    * Indicates if the status is a server error status.
    * @return True if the status is a server error status.
    */
   public boolean isServerError();

   /**
    * Indicates if the status is an error (client or server) status.
    * @return True if the status is an error (client or server) status.
    */
   public boolean isError();

}
