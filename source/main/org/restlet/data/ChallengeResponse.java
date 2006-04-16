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

package org.restlet.data;

/**
 * Authentication response sent by client to an origin server.
 */
public interface ChallengeResponse extends ControlData
{
   /**
    * Returns the scheme used.
    * @return The scheme used.
    */
   public ChallengeScheme getScheme();

   /**
    * Sets the scheme used.
    * @param scheme The scheme used.
    */
   public void setScheme(ChallengeScheme scheme);

   /**
    * Returns the credentials.
    * @return The credentials.
    */
   public String getCredentials();

   /**
    * Sets the credentials.
    * @param credentials The credentials.
    */
   public void setCredentials(String credentials);

}
