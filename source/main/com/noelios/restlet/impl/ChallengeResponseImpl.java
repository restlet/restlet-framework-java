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

package com.noelios.restlet.impl;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Authentication response sent by client to an origin server.
 */
public class ChallengeResponseImpl implements ChallengeResponse
{
   /** The challenge scheme. */
   protected ChallengeScheme scheme;

   /** The credentials. */
   protected String credentials;

   /**
    * Constructor.
    * @param scheme
    * @param credentials
    */
   public ChallengeResponseImpl(ChallengeScheme scheme, String credentials)
   {
      this.scheme = scheme;
      this.credentials = credentials;
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

   /**
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
      return "Authentication challenge response";
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Authentication response sent by client to an origin server";
   }

}
