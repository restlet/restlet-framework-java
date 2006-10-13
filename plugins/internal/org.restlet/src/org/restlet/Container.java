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

import java.util.ArrayList;
import java.util.List;

import org.restlet.spi.Factory;

/**
 * Component managing a set of connectors, virtual hosts and applications. The server connectors and 
 * virtual hosts can be shared by several applications, but the client connectors are instantiated for
 * each application in order to ensure full isolation between contained applications.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Container extends Component
{
	/** The modifiable list of virtual hosts. */ 
	private List<VirtualHost> hosts;
	
	/** The default host. */
	private VirtualHost defaultHost;

	/**
    * Default constructor. Instantiate then wrap a container provided by the current Restlet 
    * implementation.
    */
   public Container()
   {
		this(Factory.getInstance().createContainer());
   }

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Container(Context context)
	{
		super(context);
		this.hosts = null;
		this.defaultHost = VirtualHost.createDefaultHost(context);
	}
   
   /**
	 * Wrapper constructor.
	 * @param wrappedContainer The container to wrap. 
	 */
	protected Container(Container wrappedContainer)
	{
		super(wrappedContainer);
	}

	/**
	 * Returns the wrapped container.
	 * @return The wrapped container.
	 */
	public Container getWrappedContainer()
	{
		return (Container)getWrappedHandler();
	}
	
   /**
    * Returns the default virtual host.
    * @return The default virtual host.
    */
   public VirtualHost getDefaultHost()
   {
   	if(getWrappedContainer() != null)
   	{
   		return getWrappedContainer().getDefaultHost();
   	}
   	else
   	{
   		return this.defaultHost;
   	}
   }

	/**
    * Returns the modifiable list of host routers.
    * @return The modifiable list of host routers.
    */
   public List<VirtualHost> getHosts()
   {
   	if(getWrappedContainer() != null)
   	{
   		return getWrappedContainer().getHosts();
   	}
   	else
   	{
   		if(this.hosts == null) this.hosts = new ArrayList<VirtualHost>();
   		return this.hosts;
   	}
   }

   /**
    * Sets the default virtual host.
    * @param defaultHost The default virtual host.
    */
   public void setDefaultHost(VirtualHost defaultHost)
   {
   	if(getWrappedContainer() != null)
   	{
   		getWrappedContainer().setDefaultHost(defaultHost);
   	}
   	else
   	{
   		this.defaultHost = defaultHost;
   	}
   }

}
