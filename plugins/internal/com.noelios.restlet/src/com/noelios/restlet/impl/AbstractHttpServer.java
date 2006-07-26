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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.connector.AbstractServer;
import org.restlet.connector.Connector;
import org.restlet.data.ParameterList;

/**
 * Abstract HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractHttpServer extends AbstractServer
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(AbstractHttpServer.class.getCanonicalName());

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AbstractHttpServer(Component owner, ParameterList parameters, String address, int port)
   {
      super(owner, parameters, address, port);
   }
	
   /**
    * Handles the connector call.<br/>
    * The default behavior is to create an REST call and delegate it to the attached Restlet.
    * @param call The connector call.
    */
   public void handle(AbstractHttpServerCall call)
   {
   	try
   	{
   		handle(this, call, this);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Error while handling an HTTP server call: ", e.getMessage());
			logger.log(Level.INFO, "Error while handling an HTTP server call", e);
		}
   }

   /**
    * Handles an HTTP server call for a given Restlet target. 
    * @param httpServer The HTTP server connector that issued the call.
    * @param call The connector call.
    * @param target The target Restlet.
    * @throws IOException 
    */
   public static void handle(Connector httpServer, AbstractHttpServerCall call, Restlet target) throws IOException
   {
      Call restletCall = call.toUniform(httpServer);
      target.handle(restletCall);
      call.setResponse(restletCall);
      call.sendResponseHeaders();
      call.sendResponseOutput(restletCall.getOutput());
   }
   
}
