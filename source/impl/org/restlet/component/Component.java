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

import org.restlet.Element;
import org.restlet.UniformInterface;

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
   public void start();

   /** Stop hook. */
   public void stop();

   /**
    * Returns the name of this REST component.
    * @return The name of this REST component.
    */
   public String getName();

}
