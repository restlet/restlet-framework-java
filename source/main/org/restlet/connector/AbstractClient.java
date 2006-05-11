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

import org.restlet.DefaultCall;
import org.restlet.Call;
import org.restlet.data.Methods;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Abstract client connector.
 */
public abstract class AbstractClient extends AbstractConnector implements Client
{
	/** 
	 * The communication timeout during the communication with the remote server.
	 * To keep the default timeouts, lease the value to -1. 
	 */
	protected int timeout;
	
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    */
   public AbstractClient(Protocol protocol, String name)
   {
      super(protocol, name);
      this.timeout = -1;
   }

   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call get(String resourceUri)
   {
      Call call = new DefaultCall();
      call.setResourceRef(resourceUri);
      call.setMethod(Methods.GET);
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
      Call call = new DefaultCall();
      call.setResourceRef(resourceUri);
      call.setMethod(Methods.POST);
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
      Call call = new DefaultCall();
      call.setResourceRef(resourceUri);
      call.setMethod(Methods.PUT);
      call.setInput(input);
      handle(call);
      return call;
   }

   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call delete(String resourceUri)
   {
      Call call = new DefaultCall();
      call.setResourceRef(resourceUri);
      call.setMethod(Methods.DELETE);
      handle(call);
      return call;
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
   	this.timeout = timeout;
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
   	return this.timeout;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Client connector";
   }

}
