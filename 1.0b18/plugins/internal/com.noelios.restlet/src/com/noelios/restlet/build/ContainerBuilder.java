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

import org.restlet.component.Container;
import org.restlet.connector.Client;
import org.restlet.connector.GenericClient;
import org.restlet.connector.GenericServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;

/**
 * Fluent builder for Containers.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerBuilder extends ComponentBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public ContainerBuilder(ObjectBuilder parent, Container node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public Container getNode()
   {
      return (Container)super.getNode();
   }

   /**
    * Sets a property.
    * @param name The property name.
    * @param value The property value.
    * @return The current builder.
    */
   public ContainerBuilder addParameter(String name, String value)
   {
   	super.addParameter(name, value);
      return this;
   }

   /**
    * Adds a server connector to this component.
    * @param server The server connector to add.
    * @return The current builder.
    */
   public ContainerBuilder addServer(Server server)
   {
   	server.setNext(getNode());
      getNode().getServers().add(server);
      return this;
   }

   /**
    * Adds a server connector to this component.
    * @param protocol The connector protocol.
    * @param port The listening port.
    * @return The current builder.
    */
   public ContainerBuilder addServer(Protocol protocol, int port)
   {
      return addServer(new GenericServer(protocol, port, getNode()));
   }

   /**
    * Adds a server connector to this component.
    * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @return The current builder.
    */
   public ContainerBuilder addServer(Protocol protocol, String address, int port)
   {
   	return addServer(new GenericServer(protocol, address, port, getNode()));
   }

   /**
    * Adds a client connector to this component.
    * @param client The client connector to add.
    * @return The current builder.
    */
   public ContainerBuilder addClient(Client client)
   {
      getNode().getClients().add(client);
      return this;
   }

   /**
    * Adds a new client connector to this component. 
    * @param protocol The connector protocol.
    * @return The current builder.
    */
   public ContainerBuilder addClient(Protocol protocol)
   {
      return addClient(new GenericClient(protocol));
   }

}
