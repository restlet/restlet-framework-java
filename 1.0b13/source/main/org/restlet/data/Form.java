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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.Factory;

/**
 * Representation of a Web form containing submitted parameters.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Form implements Data
{
   /** The list of parameters. */
   protected List<Parameter> parameters;
   
   /**
    * Default constructor.
    */
   public Form()
   {
      this.parameters = null;
   }
   
   /**
    * Constructor.
    * @param query The web form parameters as a string.
    * @throws IOException 
    */
   public Form(String query) throws IOException
   {
   	Factory.getInstance().parseQuery(this, query);
   }

   /**
    * Construcotr.
    * @param post The posted Web form.
    * @throws IOException
    */
   public Form(Representation post) throws IOException
   {
   	Factory.getInstance().parsePost(this, post);
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
   	if(this.parameters == null) this.parameters = new ArrayList<Parameter>();
      return this.parameters;
   }

   /**
    * Adds a new parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public void addParameter(String name, String value)
   {
      getParameters().add(new Parameter(name, value));
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
   	return Factory.getInstance().format(this.parameters);
   }

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public Representation getRepresentation() 
   {
      return Factory.getInstance().createRepresentation(getQuery(), MediaTypes.APPLICATION_WWW_FORM);
   }

}
