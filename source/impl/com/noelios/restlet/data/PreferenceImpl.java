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

import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Default preference implementation.
 */
public class PreferenceImpl implements Preference
{
   /** The metadata associated with this preference. */
   private Metadata metadata;

   /** The quality/preference level. */
   private float quality;

   /** The list of parameters. */
   private List<Parameter> parameters;

   /**
    * Constructor.
    * @param metadata The associated metadata.
    */
   public PreferenceImpl(Metadata metadata)
   {
      this(metadata, 1F, null);
   }

   /**
    * Constructor.
    * @param metadata The associated metadata.
    * @param quality The quality/preference level.
    */
   public PreferenceImpl(Metadata metadata, float quality)
   {
      this(metadata, quality, null);
   }

   /**
    * Constructor.
    * @param metadata The associated metadata.
    * @param quality The quality/preference level.
    * @param parameters The list of parameters.
    */
   public PreferenceImpl(Metadata metadata, float quality, List<Parameter> parameters)
   {
      if(metadata == null)
      {
         throw new IllegalArgumentException("Metadata parameter can't be null");
      }
      else
      {
         this.metadata = metadata;
         this.quality = quality;
         this.parameters = parameters;
      }
   }

   /**
    * Returns the metadata associated with this preference.
    * @return The metadata associated with this preference.
    */
   public Metadata getMetadata()
   {
      return metadata;
   }

   /**
    * Returns the quality/preference level.
    * @return The quality/preference level.
    */
   public float getQuality()
   {
      return quality;
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
    * @return The value of the parameter with a given name.
    */
   public String getParameterValue(String name)
   {
      String result = null;

      if(getParameters() != null)
      {
         Parameter current;
         for(Iterator iter = getParameters().iterator(); iter.hasNext();)
         {
            current = (Parameter)iter.next();
            if(current.getName().equals(name))
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
      return "Metadata preference";
   }
}
