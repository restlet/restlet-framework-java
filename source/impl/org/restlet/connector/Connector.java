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

package org.restlet.connector;

import org.restlet.Element;

/**
 * Abstract mechanism that enables communication between components.<br/><br/>
 * "A connector is an abstract mechanism that mediates communication, coordination, or cooperation among components.
 * Connectors enable communication between components by transferring data elements from one interface to another
 * without changing the data." Roy T. Fielding
 * </br>
 * "Encapsulate the activities of accessing resources and transferring resource representations. The connectors present an
 * abstract interface for component communication, enhancing simplicity by providing a clean separation of concerns and hiding
 * the underlying implementation of resources and communication mechanisms" Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_2">Source dissertation</a>
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source dissertation</a>
 */
public interface Connector extends Element
{
   /** Start hook. */
   public void start();

   /** Stop hook. */
   public void stop();

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName();

}



