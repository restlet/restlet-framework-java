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

package com.noelios.restlet.ext.servlet;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.restlet.connector.ClientCall;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.ContextReference.AuthorityType;
import com.noelios.restlet.impl.ContextClient;

/**
 * Context client connector based on a Servlet context (JEE Web application context).
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletContextClient extends ContextClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ext.servlet.ServletContextClient");

   /** The Servlet context to use. */
   protected ServletContext context;

   /**
    * Constructor.
    * @param commonExtensions Indicates if the common extensions should be added.
    * @param context The Servlet context to use.
    */
   public ServletContextClient(boolean commonExtensions, ServletContext context)
   {
      super(Protocols.CONTEXT, commonExtensions);
      this.context = context;
   }
   
   /**
    * Returns the Servlet context.
    * @return The Servlet context.
    */
   public ServletContext getContext()
   {
      return this.context;
   }
   
   /**
    * Returns the supported protocols. 
    * @return The supported protocols.
    */
   public static List<Protocol> getProtocols()
   {
   	return Arrays.asList(new Protocol[]{Protocols.CONTEXT});
   }
   
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param requestUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new client call.
    */
	public ClientCall createCall(String method, String requestUri, boolean hasInput)
	{
		ClientCall result = null;
		
		try
		{
			if(getProtocol().equals(Protocols.CONTEXT))
			{
				ContextReference cr = new ContextReference(requestUri);
				
		      if(cr.getScheme().equalsIgnoreCase("context"))
		      {
		      	if(cr.getAuthorityType() == AuthorityType.WEB_APPLICATION)
		      	{
		      		result = new ServletContextCall(method, requestUri, getContext());
		      	}
		      	else
		      	{
			         throw new IllegalArgumentException("Only the Web application authority type is allowed here");
		      	}
		      }
		      else
		      {
		         throw new IllegalArgumentException("Only CONTEXT resource URIs are allowed here");
		      }
			}
			else
			{
	         throw new IllegalArgumentException("Only the CONTEXT protocol is supported by this connector");
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to create the call", e);
		}
		
		return result;
	}
	
}
