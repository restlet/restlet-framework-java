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

package com.noelios.restlet;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractChainlet;
import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.Security;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.ChallengeRequestImpl;
import com.noelios.restlet.util.Base64;

/**
 * Chainlet guarding the access to another restlet.
 * Supports the basic HTTP authentication and a
 * customizable authorization via the authorize method.
 */
public abstract class GuardChainlet extends AbstractChainlet
{
   /** Obtain a suitable logger. */
   protected Logger logger;

   /** The authentication realm. */
   protected String realm;

   /**
    * Constructor.
    * @param container The parent container.
    * @param logName The log name to used in the logging.properties file.
    * @param realm The authentication realm.
    */
   public GuardChainlet(RestletContainer container, String logName, String realm)
   {
      super(container);
      this.logger = Logger.getLogger(logName);
      this.realm = realm;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      Security security = call.getSecurity();

      if(security == null)
      {
         // Challenge the client
         security = Manager.createSecurity();
         call.setSecurity(security);
         challengeClient(call);
      }
      else
      {
         ChallengeResponse resp = security.getChallengeResponse();

         if(resp == null)
         {
            // No challenge response available, challenge the client
            challengeClient(call);
         }
         else if(resp.getScheme().equals(ChallengeSchemes.HTTP_BASIC))
         {
            try
            {
               String credentials = new String(Base64.decode(resp.getCredentials()), "US-ASCII");
               int separator = credentials.indexOf(':');

               if(separator == -1)
               {
                  // Log the blocking
                  logger.warning("Invalid credentials given by client with IP: " + call.getClientAddress());

                  // Invalid credentials
                  block(call, null);
               }
               else
               {
                  String userId = credentials.substring(0, separator);
                  String password = credentials.substring(separator + 1);

                  if(authorize(userId, password))
                  {
                     // Log the authorization
                     logger.info("User: " + userId + " was authorized for client with IP: " + call.getClientAddress());

                     // Credentials accepted, authorize access to chained restlet
                     super.handle(call);
                  }
                  else
                  {
                     // Log the blocking
                     logger.warning("User: " + userId + " failed to get authorized for client with IP: " + call.getClientAddress());

                     // Invalid credentials
                     block(call, userId);
                  }
               }
            }
            catch(UnsupportedEncodingException e)
            {
               logger.log(Level.WARNING, "Unsupported encoding error", e);
            }

         }
         else
         {
            // Authentication mechanism not supported
            challengeClient(call);
         }
      }
   }

   /**
    * Challenge a client.
    * @param call The current call.
    */
   protected void challengeClient(UniformCall call)
   {
      call.setStatus(Statuses.CLIENT_ERROR_UNAUTHORIZED);
      call.getSecurity().setChallengeRequest(new ChallengeRequestImpl(ChallengeSchemes.HTTP_BASIC, this.realm));
   }

   /**
    * Blocks a call due to invalid credentials.
    * Can be overriden to change the defaut behavior, for example to display an error page.
    * Default behavior is to re-challenge the client.
    * @param call The current call.
    * @param userId The user identifier.
    */
   protected void block(UniformCall call, String userId)
   {
      challengeClient(call);
   }

   /**
    * Indicates if the given credentials authorize access to the attached restlet.
    * @param userId The user identifier.
    * @param password The password.
    * @return True if the given credentials authorize access to the attached restlet.
    */
   protected abstract boolean authorize(String userId, String password);

}
