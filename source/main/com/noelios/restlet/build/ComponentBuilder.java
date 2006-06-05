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
import org.restlet.connector.Client;
import org.restlet.connector.DefaultClient;
import org.restlet.connector.DefaultServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;

/**
 * Fluent builder for Components.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ComponentBuilder extends RestletBuilder
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
    * Sets a property.
    * @param name The property name.
    * @param value The property value.
    * @return The current builder.
    */
   public ComponentBuilder setProperty(String name, String value)
   {
      getNode().getProperties().put(name, value);
      return this;
   }

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param server The server connector to add.
    * @return The current builder.
    */
   public ComponentBuilder addServer(String name, Server server)
   {
      getNode().getServers().put(name, server);
      return this;
   }

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @param port The listening port.
    * @return The current builder.
    */
   public ComponentBuilder addServer(String name, Protocol protocol, int port)
   {
      return addServer(name, new DefaultServer(protocol, getNode(), port));
   }

   /**
    * Adds a server connector to this component.
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @return The current builder.
    */
   public ComponentBuilder addServer(String name, Protocol protocol, String address, int port)
   {
      return addServer(name, new DefaultServer(protocol, getNode(), address, port));
   }

   /**
    * Adds a client connector to this component.
    * @param name The unique connector name.
    * @param client The client connector to add.
    * @return The current builder.
    */
   public ComponentBuilder addClient(String name, Client client)
   {
      getNode().getClients().put(name, client);
      return this;
   }

   /**
    * Adds a new client connector to this component. 
    * @param name The unique connector name.
    * @param protocol The connector protocol.
    * @return The current builder.
    */
   public ComponentBuilder addClient(String name, Protocol protocol)
   {
      return addClient(name, new DefaultClient(protocol));
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

}
