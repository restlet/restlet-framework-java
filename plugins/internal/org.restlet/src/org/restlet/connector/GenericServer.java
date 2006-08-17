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
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.spi.Factory;

/**
 * Generic server connector supporting multiples protocols.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class GenericServer extends Server
{
   /** The wrapped server. */
	private Server wrappedServer;
   
   /**
    * Constructor using the protocol's default port.
    * @param protocol The connector protocol.
    * @param next The chained Restlet.
    */
   public GenericServer(Protocol protocol, Restlet next)
   {
   	this(protocol, null, protocol.getDefaultPort(), next);
   }
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param port The listening port.
    * @param next The chained Restlet.
    */
   public GenericServer(Protocol protocol, int port, Restlet next)
   {
   	this(protocol, null, port, next);
   }
   
   /**
    * Constructor.
    * @param protocols The connector protocols.
    * @param port The listening port.
    * @param next The chained Restlet.
    */
   public GenericServer(List<Protocol> protocols, int port, Restlet next)
   {
   	this(protocols, null, port, next);
   }
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @param next The chained Restlet.
    */
   public GenericServer(Protocol protocol, String address, int port, Restlet next)
   {
   	this(Arrays.asList(protocol), address, port, next);
   }
   
   /**
    * Constructor.
    * @param protocols The connector protocols.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @param next The chained Restlet.
    */
   public GenericServer(List<Protocol> protocols, String address, int port, Restlet next)
   {
   	this.wrappedServer = Factory.getInstance().createServer(protocols, address, port);
   	setNext(next);
   }

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
  		return (this.wrappedServer != null) ? this.wrappedServer.getContext() : null;
   }

   /**
    * Returns the chained restlet.
    * @return The chained restlet.
    */
   public Restlet getNext()
   {
  		return (this.wrappedServer != null) ? this.wrappedServer.getNext() : null;
   }

	/**
	 * Indicates if a chained Restlet is set. 
	 * @return True if a chained Restlet is set. 
	 */
	public boolean hasNext()
	{
  		return (this.wrappedServer != null) ? this.wrappedServer.hasNext() : false;
	}

   /**
    * Sets the chained restlet.
    * @param next The chained restlet.
    */
   public void setNext(Restlet next)
   {
   	if(this.wrappedServer != null)
   	{
   		this.wrappedServer.setNext(next);
   	}
   }
   
   /** Starts the Restlet. */
   public void start() throws Exception
   {
   	if(this.wrappedServer != null)
   	{
   		this.wrappedServer.start();
   	}
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
   	if(this.wrappedServer != null)
   	{
   		this.wrappedServer.stop();
   	}
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	if(this.wrappedServer != null)
   	{
   		this.wrappedServer.handle(call);
   	}
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
   	if(this.wrappedServer != null)
   	{
   		return this.wrappedServer.isStarted();
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
   	if(this.wrappedServer != null)
   	{
   		return this.wrappedServer.isStopped();
   	}
   	else
   	{
   		return true;
   	}
   }
   
   /**
    * Returns the protocols supported by the connector.
    * @return The protocols supported by the connector.
    */
   public List<Protocol> getProtocols()
   {
  		return (this.wrappedServer != null) ? this.wrappedServer.getProtocols() : null;
   }

}
