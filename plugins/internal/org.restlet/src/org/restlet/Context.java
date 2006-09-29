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
import org.restlet.data.Method;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;

/**
 * Context associated with a Restlet.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Context implements ClientInterface
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
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      throw new UnsupportedOperationException("This context doesn't support the sending of client calls.");
   }
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call delete(String resourceUri)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.DELETE);
      handle(call);
      return call;
   }
   
   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call get(String resourceUri)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.GET);
      handle(call);
      return call;
   }
   
   /**
    * Gets the identified resource without its representation's content.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call head(String resourceUri)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.HEAD);
      handle(call);
      return call;
   }
   
   /**
    * Gets the options for the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call options(String resourceUri)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.OPTIONS);
      handle(call);
      return call;
   }

   /**
    * Post a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The returned uniform call.
    */
	public Call post(String resourceUri, Representation input)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.POST);
      call.setInput(input);
      handle(call);
      return call;
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The returned uniform call.
    */
   public Call put(String resourceUri, Representation input)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.PUT);
      call.setInput(input);
      handle(call);
      return call;
   }
   
   /**
    * Tests the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call trace(String resourceUri)
   {
      Call call = new Call();
      call.setResourceRef(resourceUri);
      call.setMethod(Method.TRACE);
      handle(call);
      return call;
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