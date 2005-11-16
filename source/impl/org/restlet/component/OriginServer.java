/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.component;

import org.restlet.RestletException;
import org.restlet.UniformCall;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Representation;

/**
 * Definitive source for representations of resources in a governed namespace.<br/><br/>
 * "An origin server uses a server connector to govern the namespace for a requested resource. It is the definitive source for
 * representations of its resources and must be the ultimate recipient of any request that intends to modify the value of its
 * resources. Each origin server provides a generic interface to its services as a resource hierarchy. The resource
 * implementation details are hidden behind the interface." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_3">Source dissertation</a>
 */
public interface OriginServer extends Component
{
   /**
    * Adds a server connector to this component.
    * @param server 	The server connector to add.
    * @return 			The server connector added.
    */
   public Server addServer(Server server);

   /**
    * Removes a server connector from this component.
    * @param name The name of the server connector to remove.
    */
   public void removeServer(String name);

   /**
    * Adds a client connector to this component.
    * @param client 	The client connector to add.
    * @return 			The client connector added.
    */
   public Client addClient(Client client);

   /**
    * Removes a client connector from this component.
    * @param name The name of the client connector to remove.
    */
   public void removeClient(String name);

   /**
    * Calls a client connector.
    * @param name	The name of the client connector.
    * @param call	The call to handle.
    */
   public void callClient(String name, UniformCall call) throws RestletException;
   
   /**
    * Returns a new cookie setting.
    * @param name    The name.
    * @param value   The value.
    * @return        A new cookie setting.
    */
   public CookieSetting createCookieSetting(String name, String value);

   /**
    * Creates a new form able to process the given form content.
    * @param content The form content to process.
    * @return        A new form with the given content.
    */
   public Form createForm(Representation content) throws RestletException;

}




