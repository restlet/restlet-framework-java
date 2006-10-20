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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.spi.Factory;
import org.restlet.spi.Helper;

/**
 * Component managing a set of virtual hosts.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Container extends Component
{
	/** The modifiable list of virtual hosts. */ 
	private List<VirtualHost> hosts;
	
	/** The default host. */
	private VirtualHost defaultHost;
	
	/** The helper provided by the implementation. */
	private Helper helper;

	/**
    * Default constructor. Instantiate then wrap a container provided by the current Restlet 
    * implementation.
    */
   public Container()
   {
		super(null);
		this.helper = Factory.getInstance().createHelper(this);
		setContext(this.helper.createContext());
		this.hosts = null;
		this.defaultHost = VirtualHost.createDefaultHost(getContext());
	}
	
   /**
    * Returns the default virtual host.
    * @return The default virtual host.
    */
   public VirtualHost getDefaultHost()
   {
  		return this.defaultHost;
   }

	/**
	 * Returns the helper provided by the implementation.
	 * @return The helper provided by the implementation.
	 */
	private Helper getHelper()
	{
		return this.helper;
	}

	/**
    * Returns the modifiable list of host routers.
    * @return The modifiable list of host routers.
    */
   public List<VirtualHost> getHosts()
   {
		if(this.hosts == null) this.hosts = new ArrayList<VirtualHost>();
		return this.hosts;
   }
   
   /**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
  		init(request, response);
  		getHelper().handle(request, response);
	}

   /**
    * Sets the default virtual host.
    * @param defaultHost The default virtual host.
    */
   public void setDefaultHost(VirtualHost defaultHost)
   {
  		this.defaultHost = defaultHost;
   }
	
	/** Start callback. */
	public void start() throws Exception
	{
		super.start();
		getHelper().start();
	}

	/** Stop callback. */
	public void stop() throws Exception
	{
		getHelper().stop();
		super.stop();
	}

}
