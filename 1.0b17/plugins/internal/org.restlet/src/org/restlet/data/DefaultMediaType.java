/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.data;

import java.util.Iterator;
import java.util.List;


/**
 * Media type used in representations and preferences.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultMediaType extends DefaultMetadata implements MediaType
{
   /** The list of parameters. */
   protected List<Parameter> parameters;

   /**
    * Constructor.
    * @param name The name.
    */
   public DefaultMediaType(String name)
   {
      this(name, null, "Media type or range of media types");
   }

   /**
    * Constructor.
    * @param name The name.
    * @param description The description. 
    */
   public DefaultMediaType(String name, String description)
   {
      this(name, null, description);
   }

   /**
    * Constructor.
    * @param name The name.
    * @param parameters The list of parameters.
    */
   public DefaultMediaType(String name, List<Parameter> parameters)
   {
      this(name, parameters, "Media type or range of media types");
   }

   /**
    * Constructor.
    * @param name The name.
    * @param parameters The list of parameters.
    * @param description The description.
    */
   public DefaultMediaType(String name, List<Parameter> parameters, String description)
   {
      super((name == null) ? null : name, description);
      this.parameters = parameters;
   }

   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType()
   {
      String result = null;

      if(getName() != null)
      {
         int index = getName().indexOf('/');

         // Some clients appear to use name types without subtypes
         if(index == -1)
         {
            index = getName().indexOf(';');
         }

         if(index == -1)
         {
            result = getName();
         }
         else
         {
            result = getName().substring(0, index);
         }
      }

      return result;
   }

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubType()
   {
      String result = null;

      if(getName() != null)
      {
         int slash = getName().indexOf('/');

         if(slash == -1)
         {
            // No subtype found, assume that all subtypes are accepted
            result = "*";
         }
         else
         {
            int separator = getName().indexOf(';');
            if(separator == -1)
            {
               result = getName().substring(slash + 1);
            }
            else
            {
               result = getName().substring(slash + 1, separator);
            }
         }
      }

      return result;
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
    * Indicates if a given media type is included in the current one.
    * The test is true if both types are equal or if the given media type is within the range of the 
    * current one. For example, @link{ALL} includes all media types. 
    * Parameters are ignored for this comparison. 
    * @param included The media type to test for inclusion.
    * @return True if the given media type is included in the current one.
    */
   public boolean includes(MediaType included)
   {
   	return MediaTypes.includes(this, included);
   }

}
