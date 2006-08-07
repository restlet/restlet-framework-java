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

import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.Component;
import org.restlet.component.RestletContainer;

import com.noelios.restlet.DirectoryHandler;
import com.noelios.restlet.ExtractFilter;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;

/**
 * Factory for builders.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class BuilderFactory
{
	/**
	 * Creates a Filter builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public FilterBuilder createFilterBuilder(ObjectBuilder parent, Filter node)
	{
		return new FilterBuilder(parent, node);
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
	 * Creates an DirectoryHandler builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public DirectoryHandlerBuilder createDirectoryHandlerBuilder(ObjectBuilder parent, DirectoryHandler node)
   {
   	return new DirectoryHandlerBuilder(parent, node);
   }
	
	/**
	 * Creates an ExtractFilter builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public ExtractFilterBuilder createExtractFilterBuilder(ObjectBuilder parent, ExtractFilter node)
   {
   	return new ExtractFilterBuilder(parent, node);
   }

	/**
	 * Creates a GuardFilter builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public GuardFilterBuilder createGuardFilterBuilder(ObjectBuilder parent, GuardFilter node)
   {
   	return new GuardFilterBuilder(parent, node);
   }

	/**
	 * Creates a HostRouter builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public HostRouterBuilder createHostRouterBuilder(ObjectBuilder parent, HostRouter node)
   {
   	return new HostRouterBuilder(parent, node);
   }

	/**
	 * Creates a PathRouter builder.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public RouterBuilder createRouterBuilder(ObjectBuilder parent, Router node)
   {
   	return new RouterBuilder(parent, node);
   }
   
	/**
	 * Creates an Object builder.
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
