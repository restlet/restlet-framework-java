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

package com.noelios.restlet.data;

import java.util.Iterator;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;

/**
 * Default media type implementation.
 */
public class MediaTypeImpl extends MetadataImpl implements MediaType
{
   /** The list of parameters. */
   private List<Parameter> parameters;

   /**
    * Constructor.
    * @param name The media type name.
    */
   public MediaTypeImpl(String name)
   {
      this(name, null);
   }

   /**
    * Constructor.
    * @param name 		The media type name.
    * @param parameters	The list of parameters.
    */
   public MediaTypeImpl(String name, List<Parameter> parameters)
   {
      super((name == null) ? null : name.toLowerCase());
      this.parameters = parameters;
   }

   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType()
   {
      return getName().substring(0, getName().indexOf('/'));
   }

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubtype()
   {
      int separator = getName().indexOf(';');

      if (separator == -1)
      {
         return getName().substring(getName().indexOf('/') + 1);
      }
      else
      {
         return getName().substring(getName().indexOf('/') + 1, separator);
      }
   }

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters()
   {
      return parameters;
   }

   /**
    * Returns the value of a parameter with a given name.
    * @param name The name of the parameter to return.
    * @return 		The value of the parameter with a given name.
    */
   public String getParameterValue(String name)
   {
      String result = null;

      if (getParameters() != null)
      {
         Parameter current;
         for (Iterator iter = getParameters().iterator(); iter.hasNext(); )
         {
            current = (Parameter)iter.next();
            if (current.getName().equals(name))
            {
               result = current.getValue();
            }
         }
      }

      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Media type or range of media types";
   }

}



