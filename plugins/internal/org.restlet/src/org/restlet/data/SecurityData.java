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
 * Security data related to a call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class SecurityData
{
   /** The authentication request sent by an origin server to a client. */
	private ChallengeRequest request;

   /** The authentication response sent by a client to an origin server. */
	private ChallengeResponse response;

   /** Indicates if the call came over a confidential channel. */
	private boolean confidential;

   /** The login of the authenticated caller. */
	private String login;
   
   /** The password of the authenticated caller. */
	private String password;
   
   /** The role of the authenticated caller. */
	private String role;
   
   /**
    * Constructor.
    */
   public SecurityData()
   {
      this.request = null;
      this.response = null;
      this.confidential = false;
   }

   /**
    * Returns the authentication request sent by an origin server to a client.
    * @return The authentication request sent by an origin server to a client.
    */
   public ChallengeRequest getChallengeRequest()
   {
      return this.request;
   }

   /**
    * Sets the authentication request sent by an origin server to a client.
    * @param request The authentication request sent by an origin server to a client.
    */
   public void setChallengeRequest(ChallengeRequest request)
   {
      this.request = request;
   }

   /**
    * Returns the authentication response sent by a client to an origin server.
    * @return The authentication response sent by a client to an origin server.
    */
   public ChallengeResponse getChallengeResponse()
   {
      return this.response;
   }

   /**
    * Sets the authentication response sent by a client to an origin server.
    * @param response The authentication response sent by a client to an origin server.
    */
   public void setChallengeResponse(ChallengeResponse response)
   {
      this.response = response;
   }

   /**
    * Indicates if the call came over a confidential channel
    * such as an SSL-secured connection.
    * @return True if the call came over a confidential channel.
    */
   public boolean isConfidential()
   {
      return this.confidential;
   }

   /**
    * Indicates if the call came over a confidential channel
    * such as an SSL-secured connection.
    * @param confidential True if the call came over a confidential channel.
    */
   public void setConfidential(boolean confidential)
   {
      this.confidential = confidential;
   }

   /**
    * Returns the login of the authenticated caller.
    * @return The login of the authenticated caller.
    */
   public String getLogin()
   {
   	return this.login;
   }
   
   /**
    * Returns the password of the authenticated caller.
    * @return The password of the authenticated caller.
    */
   public String getPassword()
   {
   	return this.password;
   }
   
   /**
    * Returns the role of the authenticated caller.
    * @return The role of the authenticated caller.
    */
   public String getRole()
   {
   	return this.role;
   }

   /**
    * Sets the login of the authenticated caller.
    * @param login The login of the authenticated caller.
    */
   public void setLogin(String login)
   {
   	this.login = login;
   }
   
   /**
    * Sets the password of the authenticated caller.
    * @param password The password of the authenticated caller.
    */
   public void setPassword(String password)
   {
   	this.password = password;
   }
   
   /**
    * Sets the role of the authenticated caller.
    * @param role The role of the authenticated caller.
    */
   public void setRole(String role)
   {
   	this.role = role;
   }

}
