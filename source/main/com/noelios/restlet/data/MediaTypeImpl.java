/*
 * Copyright 2005 Jérôme LOUVEL
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
    * @param name The media type name.
    * @param parameters The list of parameters.
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
   public String getSubType()
   {
      int separator = getName().indexOf(';');

      if(separator == -1)
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
      return "Media type or range of media types";
   }

}
