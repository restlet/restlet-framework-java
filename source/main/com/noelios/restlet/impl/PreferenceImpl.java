/*
 * Copyright 2005-2006 Jerome LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.impl;

import java.util.Iterator;
import java.util.List;

import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Metadata preference definition.
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
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
      return "Metadata preference";
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Metadata preference such as quality level";
   }
}
