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

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.spi.Factory;
import org.restlet.util.ClientInterface;

/**
 * Connector acting as a generic client. It internally uses one of the available connectors registered with the current
 * Restlet implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Client extends Connector implements ClientInterface
{
	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Client(Context context)
	{
		super(context);
	}

	/**
	 * Wrapper constructor.
	 * @param wrappedClient The client to wrap.
	 */
	public Client(Client wrappedClient)
	{
		super(wrappedClient);
	}
	
	/**
    * Constructor.
    * @param protocol The connector protocol.
	 */
	public Client(Protocol protocol)
	{
		this(new Context(), protocol);
	}
	
	/**
    * Constructor.
    * @param context The context.
    * @param protocol The connector protocol.
	 */
	public Client(Context context, Protocol protocol)
	{
		this(context, Arrays.asList(protocol));
	}
	
	/**
    * Constructor.
    * @param protocols The connector protocols.
	 */
	public Client(List<Protocol> protocols)
	{
		this(new Context(), protocols);
	}
	
	/**
    * Constructor.
    * @param context The context.
    * @param protocols The connector protocols.
	 */
	public Client(Context context, List<Protocol> protocols)
	{
		this(Factory.getInstance().createClient(context, protocols));
	}
	
	/**
	 * Returns the wrapped client. 
	 * @return The wrapped client.
	 */
	private Client getWrappedClient()
	{
		return (Client)getWrappedHandler();
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @return The returned response.
	 */
	public Response handle(Request request)
	{
		if(getWrappedClient() != null)
		{
			return getWrappedClient().handle(request);
		}
		else
		{
			Response response = new Response(request);
			handle(request, response);
			return response;
		}
	}
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response delete(String resourceUri)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().delete(resourceUri);
		}
		else
		{
			return handle(new Request(Method.DELETE, resourceUri));
		}
   }
   
   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response get(String resourceUri)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().get(resourceUri);
		}
		else
		{
			return handle(new Request(Method.GET, resourceUri));
		}
   }
   
   /**
    * Gets the identified resource without its representation's content.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response head(String resourceUri)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().head(resourceUri);
		}
		else
		{
			return handle(new Request(Method.HEAD, resourceUri));
		}
   }
   
   /**
    * Gets the options for the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response options(String resourceUri)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().options(resourceUri);
		}
		else
		{
			return handle(new Request(Method.OPTIONS, resourceUri));
		}
   }

   /**
    * Posts a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param entity The entity representation to post.
    * @return The response.
    */
	public Response post(String resourceUri, Representation entity)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().post(resourceUri, entity);
		}
		else
		{
			return handle(new Request(Method.POST, resourceUri, entity));
		}
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param entity The entity representation to put.
    * @return The response.
    */
   public Response put(String resourceUri, Representation entity)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().put(resourceUri, entity);
		}
		else
		{
			return handle(new Request(Method.PUT, resourceUri, entity));
		}
   }
   
   /**
    * Tests the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response trace(String resourceUri)
   {
		if(getWrappedClient() != null)
		{
			return getWrappedClient().trace(resourceUri);
		}
		else
		{
			return handle(new Request(Method.TRACE, resourceUri));
		}
   }
   
}
