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

import org.restlet.component.Component;

/**
 * Local client connector.
 * Useful to call a component that resides inside the same JVM.
 */
public class LocalClient extends LocalConnector implements Client
{
   /**
    * Constructor.
    * @param name 				The name of this REST client.
    * @param targetComponent 	The target component.
    */
   public LocalClient(String name, Component targetComponent)
   {
      super(name, targetComponent);
   }

}




