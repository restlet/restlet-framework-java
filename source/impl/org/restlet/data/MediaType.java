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

package org.restlet.data;

import java.util.List;

/**
 * The data format of a representation.
 * @see org.restlet.data.MediaTypes
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public interface MediaType extends Metadata
{
   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType();

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubtype();

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters();

   /**
    * Returns the value of a parameter with a given name.
    * @param name The name of the parameter to return.
    * @return The value of the parameter with a given name.
    */
   public String getParameterValue(String name);

}
