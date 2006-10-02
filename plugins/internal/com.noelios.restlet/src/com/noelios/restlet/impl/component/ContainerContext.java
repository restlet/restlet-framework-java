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

package com.noelios.restlet.impl.component;

import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.connector.ClientInterface;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

import com.noelios.restlet.impl.connector.LocalClient;

/**
 * Context allowing access to the container's connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerContext extends Context implements ClientInterface
{
	/** The local client. */
	private LocalClient localClient;
	
	/** The parent container. */
	private ContainerImpl container;

	/**
	 * Constructor. 
	 * @param container The parent container.
    * @param logger The logger instance of use.
	 */
	public ContainerContext(ContainerImpl container, Logger logger)
	{
		super(logger);
		this.container = container;
		this.localClient = new LocalClient();
	}
   
	/**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	Protocol protocol = call.getProtocol();
   	if(protocol == null)
   	{
   		// Attempt to guess the protocol to use
   		// from the target reference scheme
   		protocol = call.getResourceRef().getSchemeProtocol();
   	}
   	
   	if(protocol == null)
   	{
      	throw new UnsupportedOperationException("Unable to determine the protocol to use for this call.");
   	}
   	else
   	{
   		if(protocol.equals(Protocol.CONTEXT) || protocol.equals(Protocol.FILE))
   		{
   			getLocalClient().handle(call);
   		}
   		else
   		{
   			getContainer().getClientRouter().handle(call);
   		}
   	}
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
	 * Sets the parent container.
	 * @param container The parent container.
	 */
	protected void setContainer(ContainerImpl container)
	{
		this.container = container;
	}

	/**
	 * Returns the parent container.
	 * @return The parent container.
	 */
	protected ContainerImpl getContainer()
	{
		return this.container;
	}
   
   /**
    * Returns a generic client delegate.
    * @return A generic client delegate.
    */
   public ClientInterface getClient()
   {
   	return this;
   }

	/**
	 * Returns the local client.
	 * @return the local client.
	 */
	protected LocalClient getLocalClient()
	{
		return this.localClient;
	}

	/**
	 * Sets the local client.
	 * @param localClient The localClient.
	 */
	protected void setLocalClient(LocalClient localClient)
	{
		this.localClient = localClient;
	}
}
