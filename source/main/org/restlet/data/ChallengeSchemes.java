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
 * Enumeration of authentication challenge schemes.
 */
public enum ChallengeSchemes implements ChallengeScheme
{
   /** Basic HTTP scheme. */
   HTTP_BASIC,

   /** Digest HTTP scheme. */
   HTTP_DIGEST,

   /** Microsoft NTML HTTP scheme. */
   HTTP_NTLM;

   /**
    * Returns the unique name of the scheme (ex: HTTP_BASIC).
    * @return The unique name of the scheme (ex: HTTP_BASIC).
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case HTTP_BASIC:
            result = "HTTP_BASIC";
            break;
         case HTTP_DIGEST:
            result = "HTTP_DIGEST";
            break;
         case HTTP_NTLM:
            result = "HTTP_NTLM";
            break;
      }

      return result;
   }

   /**
    * Returns the technical name of the scheme (ex: BASIC).
    * @return The technical name of the scheme (ex: BASIC).
    */
   public String getTechnicalName()
   {
      String result = null;

      switch(this)
      {
         case HTTP_BASIC:
            result = "BASIC";
            break;
         case HTTP_DIGEST:
            result = "DIGEST";
            break;
         case HTTP_NTLM:
            result = "NTLM";
            break;
      }

      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case HTTP_BASIC:
            result = "Basic HTTP authentication";
            break;
         case HTTP_DIGEST:
            result = "Digest HTTP authentication";
            break;
         case HTTP_NTLM:
            result = "Microsoft's NTLM HTTP authentication";
            break;
      }

      return result;
   }

   /**
    * Indicates if the scheme is equal to a given one.
    * @param scheme The scheme to compare to.
    * @return True if the scheme is equal to a given one.
    */
   public boolean equals(ChallengeScheme scheme)
   {
      return scheme.getName().equalsIgnoreCase(getName());
   }

}
