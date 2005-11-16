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

import org.restlet.Maplet;
import org.restlet.RestletCall;
import org.restlet.UniformCall;

/**
 * Origin server that contains restlets or maplets.
 * Note that the container is a maplet itself.
 */
public interface RestletContainer extends OriginServer, Maplet
{
   /**
    * Returns a new maplet acting as a delegate for maplets.
    * Developers who need to extend the default maplets should override it.
    * @return A new maplet.
    */
   public Maplet createMapletDelegate();

   /**
    * Returns a new restlet call wrapping a given uniform call.
    * Developers who need to extend the default restlet calls should override it.
    * @return A new restlet call.
    */
   public RestletCall createRestletCall(UniformCall call);
   
}
