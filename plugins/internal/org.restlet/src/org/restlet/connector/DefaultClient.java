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

import java.util.Arrays;
import java.util.List;

import org.restlet.Call;
import org.restlet.Factory;
import org.restlet.component.Component;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Default client connector supporting multiples protocols.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultClient implements Client
{
	/** The wrapped client. */
	Client wrappedClient;
	
	/**
    * Constructor.
    * @param protocol The connector protocol.
	 */
	public DefaultClient(Protocol protocol)
	{
		this(protocol, null);
	}
	
	/**
    * Constructor.
    * @param protocols The connector protocols.
	 */
	public DefaultClient(List<Protocol> protocols)
	{
		this(protocols, null);
	}
	
	/**
    * Constructor.
    * @param protocol The connector protocol.
    * @param parameters The initial parameters.
	 */
	public DefaultClient(Protocol protocol, ParameterList parameters)
	{
		this.wrappedClient = Factory.getInstance().createClient(Arrays.asList(protocol), null, parameters);
	}
	
	/**
    * Constructor.
    * @param protocols The connector protocols.
    * @param parameters The initial parameters.
	 */
	public DefaultClient(List<Protocol> protocols, ParameterList parameters)
	{
		this.wrappedClient = Factory.getInstance().createClient(protocols, null, parameters);
	}

   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call get(String resourceUri)
   {
   	if(this.wrappedClient != null)
   	{
   		return this.wrappedClient.get(resourceUri);
   	}
   	else
   	{
   		return null;
   	}
   }
   
   /**
    * Post a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The returned uniform call.
    */
   public Call post(String resourceUri, Representation input)
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.post(resourceUri, input);
   	}
   	else
   	{
   		return null;
   	}
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The returned uniform call.
    */
   public Call put(String resourceUri, Representation input)
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.put(resourceUri, input);
   	}
   	else
   	{
   		return null;
   	}
   }
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call delete(String resourceUri)
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.delete(resourceUri);
   	}
   	else
   	{
   		return null;
   	}
   }
   
   /** Starts the Restlet. */
   public void start() throws Exception
   {
   	if(this.wrappedClient != null)
   	{
      	this.wrappedClient.start();
   	}
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
   	if(this.wrappedClient != null)
   	{
      	this.wrappedClient.stop();
   	}
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(this.wrappedClient != null)
   	{
      	this.wrappedClient.handle(call);
   	}
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.isStarted();
   	}
   	else
   	{
   		return false;
   	}
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.isStopped();
   	}
   	else
   	{
   		return true;
   	}
   }

   /**
    * Returns the owner component.
    * @return The owner component.
    */
   public Component getOwner()
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.getOwner();
   	}
   	else
   	{
   		return null;
   	}
   }

   /**
    * Sets the owner component.
    * @param owner The owner component.
    */
   public void setOwner(Component owner)
   {
   	if(this.wrappedClient != null)
   	{
   		this.wrappedClient.setOwner(owner);
   	}
   }
   
	/**
	 * Returns the modifiable map of properties.
	 * @return The modifiable map of properties.
	 */
	public ParameterList getParameters()
	{
   	if(this.wrappedClient != null)
   	{
   		return this.wrappedClient.getParameters();
   	}
   	else
   	{
   		return null;
   	}
	}

   /**
    * Returns the protocols supported by the connector.
    * @return The protocols supported by the connector.
    */
   public List<Protocol> getProtocols()
   {
   	if(this.wrappedClient != null)
   	{
      	return this.wrappedClient.getProtocols();
   	}
   	else
   	{
   		return null;
   	}
   }
   
}
