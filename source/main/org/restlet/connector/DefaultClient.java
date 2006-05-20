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

package org.restlet.connector;

import org.restlet.Call;
import org.restlet.Factory;
import org.restlet.component.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Default client connector supporting multiples protocols.
 */
public class DefaultClient implements Client
{
	/** The wrapped client. */
	Client wrappedClient;
	
	/**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
	 */
	public DefaultClient(Protocol protocol, String name)
	{
		this.wrappedClient = Factory.getInstance().createClient(protocol, name);
	}
	
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new client call.
    */
   public ClientCall createCall(String method, String resourceUri, boolean hasInput)
   {
   	return this.wrappedClient.createCall(method, resourceUri, hasInput);
   }

   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call get(String resourceUri)
   {
   	return this.wrappedClient.get(resourceUri);
   }
   
   /**
    * Post a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The returned uniform call.
    */
   public Call post(String resourceUri, Representation input)
   {
   	return this.wrappedClient.post(resourceUri, input);
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The returned uniform call.
    */
   public Call put(String resourceUri, Representation input)
   {
   	return this.wrappedClient.put(resourceUri, input);
   }
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call delete(String resourceUri)
   {
   	return this.wrappedClient.delete(resourceUri);
   }
   
   /** Starts the Restlet. */
   public void start() throws Exception
   {
   	this.wrappedClient.start();
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
   	this.wrappedClient.stop();
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	this.wrappedClient.handle(call);
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
   	return this.wrappedClient.isStarted();
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
   	return this.wrappedClient.isStopped();
   }

   /**
    * Returns the owner component.
    * @return The owner component.
    */
   public Component getOwner()
   {
   	return this.wrappedClient.getOwner();
   }

   /**
    * Sets the owner component.
    * @param owner The owner component.
    */
   public void setOwner(Component owner)
   {
   	this.wrappedClient.setOwner(owner);   	
   }

   /**
    * Returns the connector's protocol.
    * @return The connector's protocol.
    */
   public Protocol getProtocol()
   {
   	return this.wrappedClient.getProtocol();
   }
   
   /**
    * Sets the communication timeout during the communication with the remote server.
    * The unit used is the millisecond.
	 * To keep the default timeouts, lease the value to -1.
	 * For infinite timeouts, use the value 0. 
    * @param timeout The communication timeout.
    */
   public void setTimeout(int timeout)
   {
   	this.wrappedClient.setTimeout(timeout);
   }
   
   /**
    * Return the communication timeout during the communication with the remote server.
    * The unit used is the millisecond.
	 * The value -1 means that the default timeouts are used. 
	 * The value 0 means that an infinite timeout is used. 
    * @return The communication timeout.
    */
   public int getTimeout()
   {
   	return this.wrappedClient.getTimeout();
   }

   /**
    * Returns the name of this connector.
    * @return The name of this connector.
    */
   public String getName()
   {
   	return this.wrappedClient.getName();
   }

   /**
    * Sets the name of this connector.
    * @param name The name of this connector.
    */
   public void setName(String name)
   {
   	this.wrappedClient.setName(name);
   }
   
}
