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
import org.restlet.Restlet;
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
   	this((Restlet)null);
   }
   
   /**
    * Constructor.
    * @param root The root Restlet.
    */
   public Container(Restlet root)
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
    * Returns the modifiable list of applications.
    * @return The modifiable list of applications.
    */
   public List<Application> getApplications()
   {
		return getWrappedContainer().getApplications();
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
