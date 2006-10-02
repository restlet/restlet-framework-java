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

import org.restlet.component.Component;

/**
 * Fluent builder for Components.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ComponentBuilder extends ObjectBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public ComponentBuilder(ObjectBuilder parent, Component node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Component getNode()
   {
      return (Component)super.getNode();
   }

   /** 
    * Starts the component. 
    * @return The current builder.
    */
   public ComponentBuilder start() throws Exception
   {
      getNode().start();
      return this;
   }

   /**
    * Sets a property.
    * @param name The property name.
    * @param value The property value.
    * @return The current builder.
    */
   public ComponentBuilder addParameter(String name, String value)
   {
   	getNode().getContext().getParameters().add(name, value);
      return this;
   }
   
   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(int port)
   {
//      VirtualHostImpl node = new VirtualHostImpl(getNode().getContext(), port);
//      node.setUsageMode(VirtualHostImpl.USAGE_ROUTING);
//      return Builders.buildHost(this, node);
   	return null;
   }
      
   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain)
   {
//      VirtualHostImpl node = new VirtualHostImpl(getNode().getContext(), domain);
//      node.setUsageMode(VirtualHostImpl.USAGE_ROUTING);
//      return Builders.buildHost(this, node);
   	return null;
   }

   /**
    * Creates a host router for attachment in router mode. 
    * This variant of attachHost is necessary if all the configuration of the host router requires 
    * more than a domain name and a port number. This is because the router attachment pattern 
    * is computed dynamically based many properties (allowed domains, ports, etc.).  
    * @param domain The domain name. 
    * @param port The host port.
    * @return The builder for the created node.
    */
   public HostRouterBuilder createHost(String domain, int port)
   {
//      VirtualHostImpl node = new VirtualHostImpl(getNode().getContext(), domain, port);
//      node.setUsageMode(VirtualHostImpl.USAGE_ROUTING);
//      return Builders.buildHost(this, node);
   	return null;
   }
}
