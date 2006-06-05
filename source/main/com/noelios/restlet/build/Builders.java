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

import com.noelios.restlet.ExtractChainlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.HostMaplet;

/**
 * Utility methods using the current factory.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Builders
{
	/** The current factory. */
	protected static BuilderFactory factory = new BuilderFactory();
	
	/**
	 * Returns the current factory.
	 * @return The current factory.
	 */
	public static BuilderFactory getFactory()
	{
		return factory;
	}
	
	/**
	 * Sets the current factory.
	 * @param newFactory The current factory.
	 */
	public static void setFactory(BuilderFactory newFactory)
	{
		factory = newFactory;
	}
	
	/**
	 * Builds a Chainlet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public static ChainletBuilder buildChainlet(ObjectBuilder parent, Chainlet node)
	{
		return getFactory().createChainletBuilder(parent, node);
	}

	/**
	 * Builds a Component.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public static ComponentBuilder buildComponent(ObjectBuilder parent, Component node)
	{
		return getFactory().createComponentBuilder(parent, node);
	}

	/**
	 * Builds a Restlet Container.
	 */
   public static RestletContainerBuilder buildContainer()
   {
   	return getFactory().createRestletContainerBuilder(null, new RestletContainer());
   }

	/**
	 * Builds a Restlet Container.
	 * @param node The wrapped node.
	 */
   public static RestletContainerBuilder buildContainer(RestletContainer node)
   {
   	return getFactory().createRestletContainerBuilder(null, node);
   }

	/**
	 * Builds a Restlet Container.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static RestletContainerBuilder buildContainer(ObjectBuilder parent, RestletContainer node)
   {
   	return getFactory().createRestletContainerBuilder(parent, node);
   }

	/**
	 * Builds a Directory Restlet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static ExtractChainletBuilder buildExtract(ObjectBuilder parent, ExtractChainlet node)
   {
   	return getFactory().createExtractChainletBuilder(parent, node);
   }

	/**
	 * Builds a Guard Chainlet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static GuardChainletBuilder buildGuard(ObjectBuilder parent, GuardChainlet node)
   {
   	return getFactory().createGuardChainletBuilder(parent, node);
   }

	/**
	 * Builds a Host Maplet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static HostMapletBuilder buildHost(ObjectBuilder parent, HostMaplet node)
   {
   	return getFactory().createHostMapletBuilder(parent, node);
   }

	/**
	 * Builds a Maplet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static MapletBuilder buildMaplet(ObjectBuilder parent, Maplet node)
   {
   	return getFactory().createMapletBuilder(parent, node);
   }
   
	/**
	 * Builds an object.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public static ObjectBuilder buildObject(ObjectBuilder parent, Object node)
	{
		return getFactory().createObjectBuilder(parent, node);
	}

	/**
	 * Builds a Restlet.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static RestletBuilder buildRestlet(ObjectBuilder parent, Restlet node)
   {
   	return getFactory().createRestletBuilder(parent, node);
   }
	
}
