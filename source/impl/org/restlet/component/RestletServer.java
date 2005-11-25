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

/**
 * Origin server composed of restlets containers. Each container is managing a resources namespace.
 */
public interface RestletServer extends OriginServer
{
   /**
    * Adds a restlet container.
    * @param name The unique name of the container.
    * @param container The container to add.
    * @return The added container.
    */
   public RestletContainer addContainer(String name, RestletContainer container);

   /**
    * Removes a restlet container.
    * @param name The name of the container to remove.
    */
   public void removeContainer(String name);

   /**
    * Returns the default container handling direct calls to the server.
    * @return The default container.
    */
   public RestletContainer getDefaultContainer();

   /**
    * Sets the default container handling direct calls to the server.
    * @param container The default container.
    */
   public void setDefaultContainer(RestletContainer container);

}
