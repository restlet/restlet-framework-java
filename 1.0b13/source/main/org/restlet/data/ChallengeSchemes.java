/*
 * Copyright 2005-2006 Noelios Consulting.
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
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public enum ChallengeSchemes implements ChallengeScheme
{
	/** Custom scheme based on IP address or cookies or query params, etc. */
	CUSTOM,
	
   /** Basic HTTP scheme. */
   HTTP_BASIC,

   /** Digest HTTP scheme. */
   HTTP_DIGEST,

   /** Microsoft NTML HTTP scheme. */
   HTTP_NTLM,
	
   /** Plain SMTP scheme. */
   SMTP_PLAIN;

   /**
    * Returns the unique name (ex: HTTP_BASIC).
    * @return The unique name (ex: HTTP_BASIC).
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case CUSTOM:
            result = "CUSTOM";
            break;
         case HTTP_BASIC:
            result = "HTTP_BASIC";
            break;
         case HTTP_DIGEST:
            result = "HTTP_DIGEST";
            break;
         case HTTP_NTLM:
            result = "HTTP_NTLM";
            break;
         case SMTP_PLAIN:
         	result = "SMTP_PLAIN";
         	break;
      }

      return result;
   }

   /**
    * Returns the technical name (ex: Basic).
    * @return The technical name (ex: Basic).
    */
   public String getTechnicalName()
   {
      String result = null;

      switch(this)
      {
         case CUSTOM:
            result = "Custom";
            break;
         case HTTP_BASIC:
            result = "Basic";
            break;
         case HTTP_DIGEST:
            result = "Digest";
            break;
         case HTTP_NTLM:
            result = "NTLM";
            break;
         case SMTP_PLAIN:
         	result = "PLAIN";
      }

      return result;
   }

   /**
    * Returns the description.
    * @return The description.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case CUSTOM:
            result = "Custom authentication";
            break;
         case HTTP_BASIC:
            result = "Basic HTTP authentication";
            break;
         case HTTP_DIGEST:
            result = "Digest HTTP authentication";
            break;
         case HTTP_NTLM:
            result = "Microsoft's NTLM HTTP authentication";
            break;
         case SMTP_PLAIN:
         	result = "Plain SMTP authentication";
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
