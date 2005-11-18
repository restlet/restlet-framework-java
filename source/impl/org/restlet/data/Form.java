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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Submission form containing a list of parameters.
 */
public interface Form extends Representation
{
   /**
    * Reads the parameters with the given name.
    * If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return 		The parameter value or list of values.
    */
   public Object readParameter(String name) throws IOException;

   /**
    * Reads the parameters whose name is a key in the given map.
    * If a matching parameter is found, its value is put in the map.
    * If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void readParameters(Map<String, Object> parameters) throws IOException;

   /**
    * Returns a new form reader to read the list.
    * @return A new form reader to read the list.
    */
   public FormReader getFormReader() throws IOException;

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters() throws IOException;

}




