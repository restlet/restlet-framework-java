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

package com.noelios.restlet.impl;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.SecurityData;

/**
 * Security data related to a call.
 */
public class SecurityDataImpl implements SecurityData
{
   /** The authentication request sent by an origin server to a client. */
   protected ChallengeRequest request;

   /** The authentication response sent by a client to an origin server. */
   protected ChallengeResponse response;

   /** Indicates if the call came over a confidential channel. */
   protected boolean confidential;

   /**
    * Constructor.
    */
   public SecurityDataImpl()
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
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
      return "Security data";
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Security data related to a call such as authentification and confidentiality";
   }

}
