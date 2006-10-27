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
 * Authentication response sent by client to an origin server.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ChallengeResponse
{
   /** The challenge scheme. */
	private ChallengeScheme scheme;

	/** The user identifier, such as a login name or an access key. */
	private String identifier;
	
	/** The user secret, such as a password or a secret key. */
	private String secret;
	
   /** The raw credentials for custom challenge schemes. */
	private String credentials;

   /**
    * Constructor.
    * @param scheme The challenge scheme.
    * @param credentials The raw credentials for custom challenge schemes.
    */
   public ChallengeResponse(ChallengeScheme scheme, String credentials)
   {
      this.scheme = scheme;
      this.credentials = credentials;
      this.identifier = null;
      this.secret = null;
   }

   /**
    * Constructor.
    * @param scheme The challenge scheme.
    * @param identifier The user identifier, such as a login name or an access key.
    * @param secret The user secret, such as a password or a secret key.
    */
   public ChallengeResponse(ChallengeScheme scheme, String identifier, String secret)
   {
      this.scheme = scheme;
      this.credentials = null;
      this.identifier = identifier;
      this.secret = secret;
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
    * Returns the user identifier, such as a login name or an access key.
    * @return The user identifier, such as a login name or an access key.
    */
   public String getIdentifier()
   {
      return this.identifier;
   }

   /**
    * Sets the user identifier, such as a login name or an access key.
    * @param identifier The user identifier, such as a login name or an access key.
    */
   public void setIdentifier(String identifier)
   {
      this.identifier = identifier;
   }

   /**
    * Returns the user secret, such as a password or a secret key.
    * @return The user secret, such as a password or a secret key.
    */
   public String getSecret()
   {
      return this.secret;
   }

   /**
    * Sets the user secret, such as a password or a secret key.
    * @param secret The user secret, such as a password or a secret key.
    */
   public void setSecret(String secret)
   {
      this.secret = secret;
   }

}
