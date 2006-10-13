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

import org.restlet.data.Protocol;
import org.restlet.spi.Factory;

/**
 * Generic server connector. It internally uses one of the available connectors registered with the current
 * Restlet implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Server extends Connector
{
	/**
	 * Constructor.
	 * @param wrappedServer The wrapped server.
	 */
	protected Server(Server wrappedServer)
	{
		super(wrappedServer);
	}
	
   /**
    * Constructor using the protocol's default port.
    * @param protocol The connector protocol.
    * @param target The target handler.
    */
   public Server(Protocol protocol, UniformInterface target)
   {
   	this(protocol, null, protocol.getDefaultPort(), target);
   }
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param port The listening port.
    * @param target The target handler.
    */
   public Server(Protocol protocol, int port, UniformInterface target)
   {
   	this(protocol, null, port, target);
   }
   
   /**
    * Constructor.
    * @param protocols The connector protocols.
    * @param port The listening port.
    * @param target The target handler.
    */
   public Server(List<Protocol> protocols, int port, UniformInterface target)
   {
   	this(protocols, null, port, target);
   }
   
   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @param target The target handler.
    */
   public Server(Protocol protocol, String address, int port, UniformInterface target)
   {
   	this(Arrays.asList(protocol), address, port, target);
   }
   
   /**
    * Constructor.
    * @param protocols The connector protocols.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @param target The target handler.
    */
   public Server(List<Protocol> protocols, String address, int port, UniformInterface target)
   {
   	super(Factory.getInstance().createServer(protocols, address, port));
   	setTarget(target);
   }

	/**
	 * Returns the wrapped server.
	 * @return The wrapped server.
	 */
	private Server getWrappedServer()
	{
		return (Server)getWrappedConnector();
	}

   /**
    * Returns the target handler.
    * @return The target handler.
    */
   public UniformInterface getTarget()
   {
  		return (getWrappedServer() != null) ? getWrappedServer().getTarget() : null;
   }

	/**
	 * Indicates if a target handler is set. 
	 * @return True if a target handler is set. 
	 */
	public boolean hasTarget()
	{
  		return (getWrappedServer() != null) ? getWrappedServer().hasTarget() : false;
	}

   /**
    * Sets the target handler.
    * @param target The target handler.
    */
   public void setTarget(UniformInterface target)
   {
   	if(getWrappedServer() != null) getWrappedServer().setTarget(target);
   }
}
