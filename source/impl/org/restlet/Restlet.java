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

package org.restlet;

import org.restlet.component.RestletContainer;

/**
 * Handler of calls to resources or sets of resources.
 * Restlets live inside a parent container and are selected by 
 * parsing the resource's URI. Restlets are attached to other
 * restlets using maplets.
 * @see org.restlet.UniformInterface
 * @see org.restlet.Maplet
 */
public interface Restlet
{
   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException;

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer();

}
