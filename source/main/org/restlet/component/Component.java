/*
 * Copyright 2005 Jérôme LOUVEL
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

package org.restlet.component;

import java.io.IOException;

import org.restlet.Element;
import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Abstract unit of software instructions and internal state.<br/><br/> "A component is an abstract unit of
 * software instructions and internal state that provides a transformation of data via its interface." Roy T.
 * Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 * dissertation</a>
 */
public interface Component extends Element, UniformInterface
{
   /** Start hook. */
   public void start() throws Exception;

   /** Stop hook. */
   public void stop() throws Exception;

   /**
    * Returns the name of this REST component.
    * @return The name of this REST component.
    */
   public String getName();
   
   /**
    * Adds a server connector to this component.
    * @param server The server connector to add.
    * @return The server connector added.
    */
   public Server addServer(Server server);

   /**
    * Removes a server connector from this component.
    * @param name The name of the server connector to remove.
    */
   public void removeServer(String name);

   /**
    * Adds a client connector to this component.
    * @param client The client connector to add.
    * @return The client connector added.
    */
   public Client addClient(Client client);

   /**
    * Removes a client connector from this component.
    * @param name The name of the client connector to remove.
    */
   public void removeClient(String name);

   /**
    * Calls a client connector.
    * @param name The name of the client connector.
    * @param call The call to handle.
    * @throws IOException
    */
   public void callClient(String name, UniformCall call) throws IOException;

}
