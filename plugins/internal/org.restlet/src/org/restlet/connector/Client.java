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
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.restlet.spi.Factory;

/**
 * Generic client connector. It internally uses one of the available connectors registered with the current
 * Restlet implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Client extends Connector implements ClientInterface
{
	/**
	 * Constructor.
	 * @param wrappedClient The wrapped client.
	 */
	protected Client(Client wrappedClient)
	{
		super(wrappedClient);
	}
	
	/**
    * Constructor.
    * @param protocol The connector protocol.
	 */
	public Client(Protocol protocol)
	{
		this(Arrays.asList(protocol));
	}
	
	/**
    * Constructor.
    * @param protocols The connector protocols.
	 */
	public Client(List<Protocol> protocols)
	{
		super(Factory.getInstance().createClient(protocols));
	}

	/**
	 * Returns the wrapped client.
	 * @return The wrapped client.
	 */
	private Client getWrappedClient()
	{
		return (Client)getWrappedConnector();
	}

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(getWrappedClient() != null) getWrappedClient().handle(call);
   }
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call delete(String resourceUri)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().delete(resourceUri) : null;
   }

   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call get(String resourceUri)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().get(resourceUri) : null;
   }
   
   /**
    * Gets the identified resource without its representation's content.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call head(String resourceUri)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().head(resourceUri) : null;
   }
   
   /**
    * Gets the options for the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The returned uniform call.
    */
   public Call options(String resourceUri)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().options(resourceUri) : null;
   }
   
   /**
    * Post a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The returned uniform call.
    */
   public Call post(String resourceUri, Representation input)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().post(resourceUri, input) : null;
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The returned uniform call.
    */
   public Call put(String resourceUri, Representation input)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().put(resourceUri, input) : null;
   }
   
   /**
    * Tests the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The returned uniform call.
    */
   public Call trace(String resourceUri)
   {
   	return (getWrappedClient() != null) ? getWrappedClient().trace(resourceUri) : null;
   }
}
