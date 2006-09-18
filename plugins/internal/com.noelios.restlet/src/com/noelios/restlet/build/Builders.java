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

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.Application;
import org.restlet.component.Component;
import org.restlet.component.Container;

import com.noelios.restlet.DirectoryFinder;
import com.noelios.restlet.ExtractFilter;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;

/**
 * Utility methods using the current factory.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Builders
{
	/** The current factory. */
	private static BuilderFactory factory = new BuilderFactory();

	/**
	 * Creates an Application builder.
	 * @param context The container's context.
	 */
	public static ApplicationBuilder buildApplication(Context context)
	{
		return getFactory().createApplicationBuilder(context);
	}

	/**
	 * Builds an Application.
	 * @param node The wrapped node.
	 */
   public static ApplicationBuilder buildApplication(Application node)
   {
   	return getFactory().createApplicationBuilder(node);
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
	 * Builds a Container.
	 */
   public static ContainerBuilder buildContainer()
   {
   	return getFactory().createContainerBuilder(null, new Container());
   }
	
	/**
	 * Builds an Application.
	 * @param node The wrapped node.
	 */
   public static ContainerBuilder buildContainer(Container node)
   {
   	return getFactory().createContainerBuilder(null, node);
   }

	/**
	 * Builds a DirectoryHandler.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static DirectoryFinderBuilder buildDirectory(ObjectBuilder parent, DirectoryFinder node)
   {
   	return getFactory().createDirectoryHandlerBuilder(parent, node);
   }

	/**
	 * Builds an ExtractFilter.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static ExtractFilterBuilder buildExtract(ObjectBuilder parent, ExtractFilter node)
   {
   	return getFactory().createExtractFilterBuilder(parent, node);
   }

	/**
	 * Builds a Filter.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public static FilterBuilder buildFilter(ObjectBuilder parent, Filter node)
	{
		return getFactory().createFilterBuilder(parent, node);
	}

	/**
	 * Builds a GuardFilter.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static GuardFilterBuilder buildGuard(ObjectBuilder parent, GuardFilter node)
   {
   	return getFactory().createGuardFilterBuilder(parent, node);
   }

	/**
	 * Builds a HostRouter.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static HostRouterBuilder buildHost(ObjectBuilder parent, HostRouter node)
   {
   	return getFactory().createHostRouterBuilder(parent, node);
   }

	/**
	 * Builds an Object.
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
   
	/**
	 * Builds a Router.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public static RouterBuilder buildRouter(ObjectBuilder parent, Router node)
   {
   	return getFactory().createRouterBuilder(parent, node);
   }

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
	
}
