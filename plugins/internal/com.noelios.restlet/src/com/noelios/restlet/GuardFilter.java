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

package com.noelios.restlet;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;

import com.noelios.restlet.impl.util.Base64;

/**
 * Filter guarding the access to another Restlet. Currently it only supports the HTTP basic authentication 
 * scheme or custom schemes (based on cookies, query params or IP address for example).
 * @see <a href="http://www.restlet.org/tutorial#part09">Tutorial: Guarding access to sensitive resources</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class GuardFilter extends Filter
{
   /** Indicates if the guard should attempt to authenticate the caller. */
	private boolean authentication;
	
	/** Indicates if the guard should attempt to authorize the caller. */
	private boolean authorization;

	/** Map of authorizations (login/password combinations). */
	private Map<String, String> authorizations;
	
	/** The authentication scheme. */
	private ChallengeScheme scheme;
	
	/** Obtain a suitable logger. */
	private Logger logger;

   /** The authentication realm. */
	private String realm;
	
	/** The attribute name to use to store the login. */
	private String loginAttribute;
	
	/** The attribute name to use to store the password. */
	private String passwordAttribute;

   /**
    * Constructor.
    * If the authentication is not requested, the scheme and realm parameters are not necessary (pass null instead).
    * @param context The context.
    * @param logName The log name to used in the logging.properties file.
    * @param authentication Indicates if the guard should attempt to authenticate the caller.
    * @param scheme The authentication scheme to use. 
    * @param realm The authentication realm.
    * @param authorization Indicates if the guard should attempt to authorize the caller.
    */
   public GuardFilter(Context context, String logName, boolean authentication, ChallengeScheme scheme, String realm, boolean authorization)
   {
      super(context);
      this.logger = Logger.getLogger(logName);
      this.loginAttribute = "login";
      this.passwordAttribute = "password";
      this.authentication = authentication;
      this.authorizations = null;
      
      if(this.authentication && (scheme == null))
      {
      	throw new IllegalArgumentException("Please specify a challenge scheme. Use the 'None' challenge if no authentication is required.");
      }
      else
      {
      	this.scheme = scheme;
         this.realm = realm;
         this.authorization = authorization;
      }
   }

   /**
	 * Allows filtering before processing by the next handler. Does nothing by default.
    * @param request The request to handle.
    * @param response The response to update.
    */
   public void beforeHandle(Request request, Response response)
   {
   	if(this.authentication)
   	{
   		authenticate(request);
   	}
   }

	/**
	 * Handles the call by distributing it to the next handler. 
    * @param request The request to handle.
    * @param response The response to update.
	 */
   public void doHandle(Request request, Response response)
   {
		if(!this.authorization || authorize(request))
      {
      	accept(request, response);
      }
      else
      {
         reject(response);
      }
   }

   /**
    * Attempts to authenticate the caller.
    * By default, it tries to interpret the challenge response using the Filter's challenge scheme.
    * Subclasses could implement different authentication mechanisms, for example based on IP address or on
    * a session cookie.<br/>
    * The result of a successful authentication is the update of the call's security data: login, password properties.
    * Subclasses could also set the additional role property.<br/>
    * If you know that the caller has already been authenticated, you should set the challenge scheme to NONE
    * in the constructor to silently skip this step. 
    * @param request The request to authenticate.
    */
   public void authenticate(Request request)
   {
      ChallengeResponse resp = request.getChallengeResponse();

      if(this.scheme.equals(ChallengeScheme.HTTP_BASIC))
      {
         if(resp == null)
         {
            // Authentication failed, no challenge response provided, maybe first authentication attempt 
            logger.log(Level.INFO, "Authentication failed: no challenge response provided.");
         }
         else if(resp.getScheme().equals(ChallengeScheme.HTTP_BASIC))
	      {
	         try
	         {
	            String credentials = new String(Base64.decode(resp.getCredentials()), "US-ASCII");
	            int separator = credentials.indexOf(':');
	
	            if(separator == -1)
	            {
	               // Log the blocking
	               logger.warning("Invalid credentials given by client with IP: " + request.getClient().getAddress());
	            }
	            else
	            {
	            	String login = credentials.substring(0, separator);
	            	String password = credentials.substring(separator + 1);
	               request.getAttributes().put(getLoginAttribute(), login);
	               request.getAttributes().put(getPasswordAttribute(), password);
	
	               // Log the authentication result
	               logger.info("Basic HTTP authentication succeeded: login=" + login + ".");
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
	         logger.log(Level.WARNING, "Authentication failed: invalid authentication mechanism used: " + resp.getScheme().getName() + " instead of :" + this.scheme.getName() + ".");
	      }
      }
      else
      {
         // Authentication failed, scheme not supported
         logger.log(Level.WARNING, "Authentication failed: unsupported scheme used: " + this.scheme.getName() + ". Please override the authenticate method.");
      }
   }

   /**
    * Returns the map of authorizations (login/password combinations).
    * @return The map of authorizations (login/password combinations).
    */
   public Map<String, String> getAuthorizations()
   {
   	if(this.authorizations == null) this.authorizations = new TreeMap<String, String>();
   	return this.authorizations;
   }

   /**
    * Indicates if the call is authorized to pass through the Guard Filter.
    * At this point the caller should be authenticated and the security data should contain a valid login,
    * password and optionnaly a role name.<br/>
    * The application should take care of the authorization logic, based on custom criteria such as
    * checking whether the current user has the proper role or access rights.<br/>
    * By default, a call is authorized if the call's login/password couple is available in the map of authorizations. 
    * Of course, this method is meant to be overriden by subclasses requiring a custom authorization mechanism.
    * @param request The request to authorize.
    * @return True if the given credentials authorize access to the attached Restlet.
    */
   protected boolean authorize(Request request)
   {
   	boolean result = false;
   	String login = (String)request.getAttributes().get(getLoginAttribute());
   	String password = (String)request.getAttributes().get(getPasswordAttribute());
   	
   	if((login != null) && (password != null))
   	{
   		result = password.equals(getAuthorizations().get(login));
   	}
   	
   	return result;
   }
   
   /**
    * Accepts the call.
    * By default, invokes the attached Restlet.
    * @param request The request to accept.
    * @param response The response to accept.
    */
   protected void accept(Request request, Response response)
   {
   	// Invoke the chained Restlet
      super.doHandle(request, response);
   }
   
   /**
    * Rejects the call call due to a failed authentication or authorization.
    * This can be overriden to change the defaut behavior, for example to display an error page.
    * By default, if authentication is required, the challenge method is invoked, otherwise the 
    * call status is set to CLIENT_ERROR_FORBIDDEN.
    * @param response The reject response.
    */
   protected void reject(Response response)
   {
      if(this.authentication)
      {
      	challenge(response);
      }
      else
      {
      	response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
      }
   }
   
   /**
    * Sends a challenge request the caller in order to receive a (new) challenge response with caller's credentials.
    * Application using a custom authentication should override this method in order to provide a useful
    * challenging mechanism, such as displaying a login page.
    * @param response The challenge response.
    */
   protected void challenge(Response response)
   {
		if(this.scheme.equals(ChallengeScheme.HTTP_BASIC))
		{
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			response.setChallengeRequest(new ChallengeRequest(this.scheme, this.realm));
		}
		else
		{
         logger.log(Level.WARNING, "Unsupported challenging mechanism. Please override the challenge method or use a supported challenge scheme like HTTP Basic.");
		}
   }

	/**
	 * Returns the attribute name to use to store the login. The default value is "login".
	 * @return the attribute name to use to store the login.
	 */
	public String getLoginAttribute()
	{
		return loginAttribute;
	}

	/**
	 * Sets the attribute name to use to store the login.
	 * @param loginAttribute The attribute name to use to store the login.
	 */
	public void setLoginAttribute(String loginAttribute)
	{
		this.loginAttribute = loginAttribute;
	}

	/**
	 * Returns the attribute name to use to store the password. The default value is "password".
	 * @return The attribute name to use to store the password.
	 */
	public String getPasswordAttribute()
	{
		return passwordAttribute;
	}

	/**
	 * Sets the attribute name to use to store the password.
	 * @param passwordAttribute The attribute name to use to store the password.
	 */
	public void setPasswordAttribute(String passwordAttribute)
	{
		this.passwordAttribute = passwordAttribute;
	}

}
