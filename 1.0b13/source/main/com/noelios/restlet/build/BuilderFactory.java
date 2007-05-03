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

package com.noelios.restlet.build;

import org.restlet.Chainlet;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.component.RestletContainer;

import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.ExtractChainlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.HostMaplet;

/**
 * Factory for builders.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class BuilderFactory
{
	/**
	 * Creates a Chainlet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public ChainletBuilder createChainletBuilder(ObjectBuilder parent, Chainlet node)
	{
		return new ChainletBuilder(parent, node);
	}

	/**
	 * Creates a Component builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public ComponentBuilder createComponentBuilder(ObjectBuilder parent, Component node)
	{
		return new ComponentBuilder(parent, node);
	}

	/**
	 * Creates a Directory Restlet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public DirectoryRestletBuilder createDirectoryRestletBuilder(ObjectBuilder parent, DirectoryRestlet node)
   {
   	return new DirectoryRestletBuilder(parent, node);
   }

	/**
	 * Creates an Extract Chainlet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public ExtractChainletBuilder createExtractChainletBuilder(ObjectBuilder parent, ExtractChainlet node)
   {
   	return new ExtractChainletBuilder(parent, node);
   }

	/**
	 * Creates a Guard Chainlet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public GuardChainletBuilder createGuardChainletBuilder(ObjectBuilder parent, GuardChainlet node)
   {
   	return new GuardChainletBuilder(parent, node);
   }

	/**
	 * Creates a Host Maplet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public HostMapletBuilder createHostMapletBuilder(ObjectBuilder parent, HostMaplet node)
   {
   	return new HostMapletBuilder(parent, node);
   }

	/**
	 * Creates a Maplet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public MapletBuilder createMapletBuilder(ObjectBuilder parent, Maplet node)
   {
   	return new MapletBuilder(parent, node);
   }
   
	/**
	 * Creates an object builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public ObjectBuilder createObjectBuilder(ObjectBuilder parent, Object node)
	{
		return new ObjectBuilder(parent, node);
	}

	/**
	 * Creates a Restlet builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public RestletBuilder createRestletBuilder(ObjectBuilder parent, Restlet node)
   {
   	return new RestletBuilder(parent, node);
   }

	/**
	 * Creates a Restlet Container builder.
	 */
   public RestletContainerBuilder createRestletContainerBuilder()
   {
   	return new RestletContainerBuilder(null, new RestletContainer());
   }

	/**
	 * Creates a Restlet Container builder.
	 * @param containerName The name of the new container.
	 */
   public RestletContainerBuilder createRestletContainerBuilder(String containerName)
   {
   	return new RestletContainerBuilder(null, new RestletContainer(containerName));
   }

	/**
	 * Creates a Restlet Container builder.
	 * @param node The wrapped node.
	 */
   public RestletContainerBuilder createRestletContainerBuilder(RestletContainer node)
   {
   	return new RestletContainerBuilder(null, node);
   }

	/**
	 * Creates a Restlet Container builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public RestletContainerBuilder createRestletContainerBuilder(ObjectBuilder parent, RestletContainer node)
   {
   	return new RestletContainerBuilder(parent, node);
   }

}
