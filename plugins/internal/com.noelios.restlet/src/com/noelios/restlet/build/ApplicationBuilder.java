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

import org.restlet.component.Application;
import org.restlet.component.Container;

/**
 * Fluent builder for Restlet Containers.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationBuilder extends ComponentBuilder
{
	/**
	 * Constructor for new containers.
	 * @param container The parent container.
	 */
	public ApplicationBuilder(Container container)
	{
		this(null, new Application(container));
	}
	
	/**
	 * Constructor for existing containers.
	 * @param node The wrapped node.
	 */
	public ApplicationBuilder(Application node)
	{
		this(null, node);
	}
	
	/**
	 * Constructor for existing containers.
	 * @param parent The parent builder.
	 */
	public ApplicationBuilder(ContainerBuilder parent)
	{
		super(parent, new Application(parent.getNode()));
	}
	
	/**
	 * Constructor for existing containers.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public ApplicationBuilder(ContainerBuilder parent, Application node)
	{
		super(parent, node);
	}
	
   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
	public Application getNode()
	{
		return (Application)super.getNode();
	}

   /**
    * Sets a property.
    * @param name The property name.
    * @param value The property value.
    * @return The current builder.
    */
   public ApplicationBuilder addParameter(String name, String value)
   {
   	super.addParameter(name, value);
      return this;
   }

   /** 
    * Starts the component. 
    * @return The current builder.
    */
   public ApplicationBuilder start() throws Exception
   {
   	getNode().start();
      return this;
   }


}
