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

package com.noelios.restlet.impl.connector;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.connector.Client;
import org.restlet.connector.Connector;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Connector that initiates communication by making a request. By default, the handle(UniformCall)
 * method converts the call received into a connector call and handle it.<br/><br/>"The primary connector types are
 * client and server. The essential difference between the two is that a client initiates communication by
 * making a request, whereas a server listens for connections and responds to requests in order to supply
 * access to its services. A component may include both client and server connectors." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class ClientImpl extends Client
{   
   /** The context. */
	private Context context;
   
   /** The supported protocols. */
   private List<Protocol> protocols;

	/** Indicates if the restlet was started. */
   private boolean started;
   
   /**
    * Constructor that uses the class name as the logger name.
    */
   public ClientImpl()
   {
   	super((ClientImpl)null);
   	setContext(new Context(getClass().getCanonicalName()));
   }
   
   /**
    * Constructor.
    * @param loggerName The logger name to use in the context.
    */
   public ClientImpl(String loggerName)
   {
   	this(new Context(loggerName));
   }

   /**
	 * Conctructor.
	 * @param context The context.
	 */
	public ClientImpl(Context context)
	{
   	super((ClientImpl)null);
		this.context = context;
		this.started = false;
		this.protocols = null;
	}

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return this.context;
   }

   /**
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
      this.context = context;
   }

	/**
	 * Returns the wrapped connector.
	 * @return The wrapped connector.
	 */
	protected Connector getWrappedConnector()
	{
		return this;
	}

   /** Starts the Restlet. */
   public void start() throws Exception
   {
      this.started = true;
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return !this.started;
   }
   
   /**
    * Returns the protocols supported by the connector.
    * @return The protocols supported by the connector.
    */
   public List<Protocol> getProtocols()
   {
      if(this.protocols == null)
      {
      	this.protocols = new ArrayList<Protocol>();
      }
      
      return this.protocols;
   }
   
	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @return The returned response.
	 */
	public Response handle(Request request)
	{
		Response response = new Response(request);
		handle(request, response);
		return response;
	}

	/**
	 * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	public abstract void handle(Request request, Response response);
   
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response delete(String resourceUri)
   {
      return handle(new Request(Method.DELETE, resourceUri));
   }
   
   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response get(String resourceUri)
   {
      return handle(new Request(Method.GET, resourceUri));
   }
   
   /**
    * Gets the identified resource without its representation's content.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response head(String resourceUri)
   {
      return handle(new Request(Method.HEAD, resourceUri));
   }
   
   /**
    * Gets the options for the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response options(String resourceUri)
   {
      return handle(new Request(Method.OPTIONS, resourceUri));
   }

   /**
    * Posts a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The response.
    */
	public Response post(String resourceUri, Representation input)
   {
      return handle(new Request(Method.POST, resourceUri, input));
   }

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The response.
    */
   public Response put(String resourceUri, Representation input)
   {
      return handle(new Request(Method.PUT, resourceUri, input));
   }
   
   /**
    * Tests the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response trace(String resourceUri)
   {
      return handle(new Request(Method.TRACE, resourceUri));
   }
}
