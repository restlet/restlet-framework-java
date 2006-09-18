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

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;

/**
 * Connector that listens for connections and responds to requests. By default, the handle(UniformCall)
 * method delegates the call received to the target restlet<br/><br/>"The primary connector types are
 * client and server. The essential difference between the two is that a client initiates communication by
 * making a request, whereas a server listens for connections and responds to requests in order to supply
 * access to its services. A component may include both client and server connectors." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServerImpl extends Server
{
   /** The context. */
	private Context context;
	
	/** The target Restlet. */
   private Restlet target;
   
   /** The supported protocols. */
   private List<Protocol> protocols;

	/** Indicates if the restlet was started. */
   private boolean started;
   
   /**
    * Constructor that uses the class name as the logger name.
    */
   public ServerImpl()
   {
   	super((ServerImpl)null);
   	setContext(new Context(getClass().getCanonicalName()));
   }
   
   /**
    * Constructor.
    * @param loggerName The logger name to use in the context.
    */
   public ServerImpl(String loggerName)
   {
   	this(new Context(loggerName));
   }

   /**
	 * Conctructor.
	 * @param context The context.
	 */
	public ServerImpl(Context context)
	{
   	super((ServerImpl)null);
		this.context = context;
		this.started = false;
		this.protocols = null;
		this.target = null;
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
    * Handles a call.<br/>
    * The default behavior is to ask the target Restlet to handle the call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(getTarget() != null) 
   	{
   		getTarget().handle(call);
   	}
   	else
   	{
   		call.setStatus(Status.SERVER_ERROR_INTERNAL);
   		getContext().getLogger().warning("Unable to find a chained Restlet for the Server connector.");
   	}
   }

   /**
    * Returns the target Restlet.
    * @return The target Restlet.
    */
   public Restlet getTarget()
   {
      return this.target;
   }

   /**
    * Indicates if a target Restlet is set.
    * @return True if a target Restlet is set.
    */
   public boolean hasTarget()
   {
      return getTarget() != null;
   }

   /**
    * Sets the target Restlet.
    * @param target The target Restlet.
    */
   public void setTarget(Restlet target)
   {
      this.target = target;
   }

}
