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
public class Form extends ParameterList
{
	/**
	 * Empty constructor.
	 */
	public Form()
	{
		super();
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
    * Constructor.
    * @param post The posted Web form.
    * @throws IOException
    */
   public Form(Representation post) throws IOException
   {
   	Factory.getInstance().parsePost(this, post);
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
      for(Iterator<Parameter> iter = iterator(); iter.hasNext();)
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

   public String getQueryString()
   {
    	try
     	{
    		return urlEncode();
   	}
   	catch(IOException ioe)
   	{
   		return null;
   	}
   }

   /**
    * URL encodes the form. 
    * @return The encoded form.
    * @throws IOException
    */
   public String urlEncode() throws IOException
   {
	      StringBuilder sb = new StringBuilder();
	      for(int i = 0; i < size(); i++)
	      {
	         if(i > 0) sb.append('&');
	         get(i).urlEncode(sb);
	      }
	      return sb.toString();
   }

   /**
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public Representation getWebForm() 
   {
      return Factory.getInstance().createRepresentation(getQueryString(), MediaTypes.APPLICATION_WWW_FORM);
   }

}
