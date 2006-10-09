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

package org.restlet.component;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.VirtualHost;
import org.restlet.spi.Factory;

/**
 * Component managing a set of connectors, virtual hosts and applications. The server connectors and 
 * virtual hosts can be shared by several applications, but the client connectors are instantiated for
 * each application in order to ensure full isolation between contained applications.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Container extends Component
{
	/**
	 * Constructor that adds a default local connector and uses a local logger.
	 * @param wrappedContainer The wrapped container. 
	 */
	protected Container(Container wrappedContainer)
	{
		super(wrappedContainer);
	}
   
   /**
    * Constructor.
    */
   public Container()
   {
		this(Factory.getInstance().createContainer());
   }
   
   /**
    * Returns the wrapped container.
    * @return The wrapped container.
    */
	private Container getWrappedContainer()
	{
		return (Container)getWrappedComponent();
	}

	/**
	 * Returns the modifiable list of client connectors.
	 * @return The modifiable list of client connectors.
	 */
	public ClientList getClients()
	{
		return getWrappedContainer().getClients();
	}

   /**
    * Returns the modifiable list of host routers.
    * @return The modifiable list of host routers.
    */
   public List<VirtualHost> getHosts()
   {
		return getWrappedContainer().getHosts();
   }

   /**
    * Returns the local virtual host.
    * @return The local virtual host.
    */
   public VirtualHost getLocalHost()
   {
		return getWrappedContainer().getLocalHost();
   }

	/**
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		return getWrappedContainer().getServers();
	}
	
   /**
    * Handles a request.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response)
   {
		getWrappedContainer().handle(request, response);
   }

   /**
    * Sets the local virtual host.
    * @param localHost The local virtual host.
    */
   public void setLocalHost(VirtualHost localHost)
   {
		getWrappedContainer().setLocalHost(localHost);
   }

}
