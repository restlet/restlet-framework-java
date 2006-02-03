/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.EmptyValue;
import org.restlet.data.Form;
import org.restlet.data.MediaTypes;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

import com.noelios.restlet.util.FormUtils;

/**
 * Representation of a Web form containing submitted parameters.
 */
public class FormImpl implements Form
{
   /** The list of parameters. */
   protected List<Parameter> parameters;
   
   /**
    * Default constructor.
    */
   public FormImpl()
   {
      this.parameters = new ArrayList<Parameter>();
   }
   
   /**
    * Constructor.
    * @param query The web form parameters as a string.
    * @throws IOException 
    */
   public FormImpl(String query) throws IOException
   {
      this.parameters = FormUtils.getParameters(query);
   }

   /**
    * Construcotr.
    * @param content The web form parameters as a representation.
    * @throws IOException
    */
   public FormImpl(Representation content) throws IOException
   {
      this.parameters = FormUtils.getParameters(content);
   }

   /**
    * Gets the parameters with the given name.<br/>
    * If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    */
   public List<Parameter> getParameters(String name) 
   {
      List<Parameter> result = null;
      
      Parameter current;
      for(Iterator<Parameter> iter = getParameters().iterator(); (result == null) && iter.hasNext();)
      {
         current = iter.next();
         
         if(current.getName().equals(name))
         {
            if(result == null) result = new ArrayList<Parameter>();
            result.add(current);
         }
      }
      
      return result;
   }

   /**
    * Gets the first parameter with the given name.
    * @param name The parameter name to match.
    * @return The parameter value.
    */
   public Parameter getFirstParameter(String name) 
   {
      Parameter result = null;
      Parameter current;
      for(Iterator<Parameter> iter = getParameters().iterator(); (result == null) && iter.hasNext();)
      {
         current = iter.next();
         
         if(current.getName().equals(name))
         {
            result = current;
         }
      }
      
      return result;
   }

   /**
    * Returns the modifiable list of parameters.
    * @return The modifiable list of parameters.
    */
   public List<Parameter> getParameters()
   {
      return this.parameters;
   }

   /**
    * Adds a new parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public void addParameter(String name, String value)
   {
      getParameters().add(new ParameterImpl(name, value));
   }

   /**
    * Removes parameters with a given name.
    * @param name The name of the parameters to remove.
    */
   public void removeParameters(String name)
   {
      Parameter current;
      for(Iterator<Parameter> iter = getParameters().iterator(); iter.hasNext();)
      {
         current = iter.next();
         
         if(current.getName().equals(name))
         {
            iter.remove();
         }
      }
   }
   
   /**
    * Gets the parameters whose name is a key in the given map.<br/>
    * If a matching parameter is found, its value is put in the map.<br/>
    * If multiple values are found, a list is created and set in the map.
    * @param params The parameters map controlling the reading.
    */
   @SuppressWarnings("unchecked")
   public void getParameters(Map<String, Object> params) 
   {
      Parameter param;
      Object currentValue = null;
      for(Iterator<Parameter> iter = getParameters().iterator(); iter.hasNext();)
      {
         param = iter.next();
         
         if(params.containsKey(param.getName()))
         {
            currentValue = params.get(param.getName());

            if(currentValue != null)
            {
               List<Object> values = null;

               if(currentValue instanceof List)
               {
                  // Multiple values already found for this parameter
                  values = (List<Object>)currentValue;
               }
               else
               {
                  // Second value found for this parameter
                  // Create a list of values
                  values = new ArrayList<Object>();
                  values.add(currentValue);
                  params.put(param.getName(), values);
               }

               if(param.getValue() == null)
               {
                  values.add(new EmptyValue());
               }
               else
               {
                  values.add(param.getValue());
               }
            }
            else
            {
               if(param.getValue() == null)
               {
                  params.put(param.getName(), new EmptyValue());
               }
               else
               {
                  params.put(param.getName(), param.getValue());
               }
            }
         }
      }
      
   }

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public String getQuery() 
   {
      try
      {
         return FormUtils.format(this.parameters);
      }
      catch(IOException e)
      {
         return null;
      }
   }

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public Representation getRepresentation() 
   {
      return new StringRepresentation(getQuery(), MediaTypes.APPLICATION_WWW_FORM);
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Form data";
   }

}
