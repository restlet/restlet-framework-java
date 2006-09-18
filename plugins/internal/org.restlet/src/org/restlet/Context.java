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

package org.restlet;

import java.util.logging.Logger;

import org.restlet.connector.ClientInterface;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;

/**
 * Context associated with a Restlet.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Context
{
   /** The modifiable list of parameters. */
   private ParameterList parameters;
   
   /** The logger instance to use. */
   private Logger logger;

   /**
    * Constructor.
    * @param loggerName The name of the logger to use.
    */
   public Context(String loggerName)
   {
   	this(Logger.getLogger(loggerName));
   }

   /**
    * Constructor.
    * @param logger The logger instance of use.
    */
   public Context(Logger logger)
   {
   	this.logger = logger;
   }

   /**
    * Returns a client delegate for the given protocol.
    * @param protocol The protocol required.
    * @return A client delegate for the given protocol or null if it isn't available.
    */
   public ClientInterface getClient(Protocol protocol)
   {
      throw new UnsupportedOperationException("This context doesn't dispatch calls");
   }
   
   /**
    * Gets the client delegate for the call's target resource based on the reference's scheme protocol,
    * then use it to handle the given call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	ClientInterface client = getClient(call.getResourceRef().getSchemeProtocol());
   	
   	if(client != null)
   	{
   		client.handle(call);
   	}
   	else
   	{
         throw new RuntimeException("Unable to find a client delegate for the protocol: " + call.getResourceRef().getSchemeProtocol().getName());
   	}
   }
   
   /**
    * Returns the logger.
    * @return The logger.
    */
   public Logger getLogger()
   {
      return this.logger;
   }

   /**
    * Sets the logger.
    * @param logger The logger.
    */
   protected void setLogger(Logger logger)
   {
      this.logger = logger;
   }
   
   /**
    * Returns the modifiable list of parameters.
    * @return The modifiable list of parameters.
    */
   public ParameterList getParameters()
   {
      if(this.parameters == null) this.parameters = new ParameterList();
      return this.parameters;
   }
}