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

import org.restlet.Call;
import org.restlet.UniformInterface;
import org.restlet.spi.Factory;

/**
 * Component containing a set of connectors and applications. The connectors are shared by the 
 * applications and the container attemps to isolate each application from each other.
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
   	this((UniformInterface)null);
   }
   
   /**
    * Constructor.
    * @param root The root handler.
    */
   public Container(UniformInterface root)
   {
		this(Factory.getInstance().createContainer(root));
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
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		return getWrappedContainer().getServers();
	}

   /**
    * Returns the default virtual host.
    * @return The default virtual host.
    */
   public Host getDefaultHost()
   {
		return getWrappedContainer().getDefaultHost();
   }

   /**
    * Returns the modifiable list of virtual hosts.
    * @return The modifiable list of virtual hosts.
    */
   public List<Host> getVirtualHosts()
   {
		return getWrappedContainer().getVirtualHosts();
   }
   
   /**
    * Handles a direct call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
		getWrappedContainer().handle(call);
   }

   /**
    * Sets the default virtual host.
    * @param defaultHost The default virtual host.
    */
   public void setDefaultHost(Host defaultHost)
   {
		getWrappedContainer().setDefaultHost(defaultHost);
   }

   /**
    * Start hook. Starts all containers.
    */
   public void start() throws Exception
   {
		getWrappedContainer().start();
   }

   /**
    * Stop hook. Stops all containers.
    */
   public void stop() throws Exception
   {
		getWrappedContainer().stop();
   }

}
