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
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.SecurityData;
import org.restlet.data.Statuses;

import com.noelios.restlet.impl.ChallengeRequestImpl;
import com.noelios.restlet.util.Base64;

/**
 * Chainlet guarding the access to another handler (Restlet, Chainlet, Maplet, etc.).<br/>
 * Currently only supports the HTTP basic authentication scheme and a custome schemes (based on cookies, query params or IP address for example).
 * @see <a href="http://www.restlet.org/tutorial#part09">Tutorial: Guarding access to sensitive resources</a>
 */
public abstract class GuardChainlet extends AbstractChainlet
{
	/** The authentication scheme. */
	protected ChallengeScheme scheme;
	
	/** Obtain a suitable logger. */
   protected Logger logger;

   /** The authentication realm. */
   protected String realm;

   /**
    * Constructor.
    * @param container The parent container.
    * @param scheme The authentication scheme to use or null if this is a custom scheme (cookies, query params, etc.). 
    * @param logName The log name to used in the logging.properties file.
    * @param realm The authentication realm.
    */
   public GuardChainlet(RestletContainer container, ChallengeScheme scheme, String logName, String realm)
   {
      super(container);
      this.scheme = scheme;
      this.logger = Logger.getLogger(logName);
      this.realm = realm;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      if(authenticate(call))
      {
         // Authentication succeeded
      	// Invoke the chained Restlet
         super.handle(call);
      }
      else
      {
         // Authentication failed
         block(call);
      }
   }

   /**
    * Authenticate the call using the specified challenge scheme or a custom scheme.
    * By default, implement the standard challenge scheme (HTTP basic) then invoke the other abstract authenticate method. 
    * @param call The call to authenticate.
    * @return True if the call was authenticated.
    */
   protected boolean authenticate(UniformCall call)
   {
   	boolean result = false;
   
   	if(this.scheme == null)
   	{
			result = authenticate(call, null, null);

			if(result)
         {
            // Log the authentication
            logger.info("User authenticated for client with IP: " + call.getClientAddress());
         }
         else
         {
            // Log the blocking
            logger.warning("User couldn't be authenticated for client with IP: " + call.getClientAddress());
         }
   	}
   	else
   	{
	      SecurityData security = call.getSecurity();
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
	               block(call);
	            }
	            else
	            {
	               String userId = credentials.substring(0, separator);
	               String password = credentials.substring(separator + 1);
	               result = authenticate(call, userId, password); 
	               
	               if(result)
	               {
	                  // Log the authentication
	                  logger.info("User: " + userId + " authenticated for client with IP: " + call.getClientAddress());
	               }
	               else
	               {
	                  // Log the blocking
	                  logger.warning("User: " + userId + " couldn't be authenticated for client with IP: " + call.getClientAddress());
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
            logger.log(Level.WARNING, "Unsupported authentication mechanism: " + resp.getScheme().getName() + ". Challenging the client again.");
	         challengeClient(call);
	      }
   	}
   	
   	return result;
   }

   /**
    * Authenticate the current user based on the given credentials.
    * If the authentication succeed, then the attached handler will be invoked.
    * The application should take care of authorizing and authenticated user based on application criteria 
    * like the current user role. In case an unauthorized action is requested, a Statuses.CLIENT_ERROR_UNAUTHORIZED
    * status should be returned.
    * @param call The current call.
    * @param userId The user identifier.
    * @param password The password.
    * @return True if the given credentials authorize access to the attached handler.
    */
   protected abstract boolean authenticate(UniformCall call, String userId, String password);
   
   /**
    * Challenge a client.
    * @param call The current call.
    */
   protected void challengeClient(UniformCall call)
   {
   	if(this.scheme != null)
   	{
   		call.setStatus(Statuses.CLIENT_ERROR_UNAUTHORIZED);
   		call.getSecurity().setChallengeRequest(new ChallengeRequestImpl(this.scheme, this.realm));
   	}
   	else
   	{
         logger.log(Level.WARNING, "Unspecified client challenging mechanism. Please override the challengeClient method or use a standard challenge scheme.");
   	}
   }

   /**
    * Blocks a call due to invalid credentials.
    * This can be overriden to change the defaut behavior, for example to display an error page.
    * Default behavior is to challenge the client.
    * @param call The current call.
    */
   protected void block(UniformCall call)
   {
      challengeClient(call);
   }

}
