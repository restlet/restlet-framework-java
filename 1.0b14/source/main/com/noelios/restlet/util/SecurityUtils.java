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

package com.noelios.restlet.util;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.DefaultChallengeScheme;

/**
 * Security data manipulation utilities.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class SecurityUtils
{
   /**
    * Formats a challenge request as a HTTP header value.
    * @param request The challenge request to format.
    * @return The authenticate header value.
    */
   public static String format(ChallengeRequest request)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(request.getScheme().getTechnicalName());
      sb.append(" realm=\"").append(request.getRealm()).append('"');
      return sb.toString();
   }

   /**
    * Formats a challenge response as a HTTP header value.
    * @param response The challenge response to format.
    * @return The authorization header value.
    */
   public static String format(ChallengeResponse response)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(response.getScheme().getTechnicalName()).append(' ');
      sb.append(response.getCredentials());
      return sb.toString();
   }

   /**
    * Parses an authenticate header into a challenge request.
    * @param header The HTTP header value to parse.
    * @return The parsed challenge request.
    */
   public static ChallengeRequest parseRequest(String header)
   {
      ChallengeRequest result = null;

      if(header != null)
      {
         int space = header.indexOf(' ');

         if(space != -1)
         {
            String scheme = header.substring(0, space);
            String realm = header.substring(space + 1);
            int equals = realm.indexOf('=');
            String realmValue = realm.substring(equals + 2, realm.length() - 1);
            result = new ChallengeRequest(new DefaultChallengeScheme("HTTP_" + scheme, scheme), realmValue);
         }
      }
      
      return result;
   }

   /**
    * Parses an authorization header into a challenge response.
    * @param header The HTTP header value to parse.
    * @return The parsed challenge response.
    */
   public static ChallengeResponse parseResponse(String header)
   {
      ChallengeResponse result = null;

      if(header != null)
      {
         int space = header.indexOf(' ');

         if(space != -1)
         {
            String scheme = header.substring(0, space);
            String credentials = header.substring(space + 1);
            result = new ChallengeResponse(new DefaultChallengeScheme("HTTP_" + scheme, scheme), credentials);
         }
      }
      
      return result;
   }
   
}
