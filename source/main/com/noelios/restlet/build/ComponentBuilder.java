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
	 * @param node The wrapped component.
	 */
   public ComponentBuilder(DefaultBuilder parent, Component node)
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
    * Adds a server connector to this component.
    * @param server The server connector to add.
    * @return The current builder.
    */
   public ComponentBuilder addServer(Server server)
   {
      getNode().addServer(server);
      return this;
   }

   /**
    * Adds a server connector to this component.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param port The listening port.
    * @return The current builder.
    */
   public ComponentBuilder addServer(Protocol protocol, String name, int port)
   {
      return addServer(new DefaultServer(protocol, name, getNode(), port));
   }

   /**
    * Adds a server connector to this component.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
    * @return The current builder.
    */
   public ComponentBuilder addServer(Protocol protocol, String name, String address, int port)
   {
      return addServer(new DefaultServer(protocol, name, getNode(), address, port));
   }

   /**
    * Adds a client connector to this component.
    * @param client The client connector to add.
    * @return The current builder.
    */
   public ComponentBuilder addClient(Client client)
   {
      getNode().addClient(client);
      return this;
   }

   /**
    * Adds a new client connector to this component. 
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @return The current builder.
    */
   public ComponentBuilder addClient(Protocol protocol, String name)
   {
      return addClient(new DefaultClient(protocol, name));
   }

   /**
    * Sets an initialization parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    * @return The current builder.
    */
   public ComponentBuilder setInitParameter(String name, String value)
   {
      getNode().getInitParameters().put(name, value);
      return this;
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
