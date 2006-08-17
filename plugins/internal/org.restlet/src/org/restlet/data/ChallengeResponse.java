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

import org.restlet.spi.Factory;

/**
 * Authentication response sent by client to an origin server.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ChallengeResponse
{
   /** The challenge scheme. */
	private ChallengeScheme scheme;

   /** The credentials. */
	private String credentials;

   /**
    * Constructor.
    * @param scheme The challenge scheme.
    * @param credentials The credentials to use.
    */
   public ChallengeResponse(ChallengeScheme scheme, String credentials)
   {
      this.scheme = scheme;
      this.credentials = credentials;
   }

   /**
    * Constructor.
    * @param scheme The challenge scheme.
    * @param userId The user identifier to use.
    * @param password The password to use.
    */
   public ChallengeResponse(ChallengeScheme scheme, String userId, String password)
   {
      this.scheme = scheme;
      Factory.getInstance().setCredentials(this, userId, password);
   }

   /**
    * Returns the scheme used.
    * @return The scheme used.
    */
   public ChallengeScheme getScheme()
   {
      return this.scheme;
   }

   /**
    * Sets the scheme used.
    * @param scheme The scheme used.
    */
   public void setScheme(ChallengeScheme scheme)
   {
      this.scheme = scheme;
   }

   /**
    * Returns the credentials.
    * @return The credentials.
    */
   public String getCredentials()
   {
      return this.credentials;
   }

   /**
    * Sets the credentials.
    * @param credentials The credentials.
    */
   public void setCredentials(String credentials)
   {
      this.credentials = credentials;
   }

}
