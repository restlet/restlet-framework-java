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

/**
 * Uniform interface between all architecture components.<br/><br/> "The central feature that distinguishes
 * the REST architectural style from other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions is improved. Implementations
 * are decoupled from the services they provide, which encourages independent evolvability." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 * dissertation</a>
 */
public interface UniformInterface
{
   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call);
}
