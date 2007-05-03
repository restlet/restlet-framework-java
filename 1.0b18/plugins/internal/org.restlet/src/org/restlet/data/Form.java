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

import org.restlet.spi.Factory;

/**
 * Form which is a specialized modifiable list of parameters.
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
    * @param queryString The Web form parameters as a string.
    * @throws IOException 
    */
   public Form(String queryString) throws IOException
   {
   	Factory.getInstance().parse(this, queryString);
   }

   /**
    * Constructor.
    * @param webForm The URL encoded Web form.
    * @throws IOException
    */
   public Form(Representation webForm) throws IOException
   {
   	Factory.getInstance().parse(this, webForm);
   }

   /**
    * Formats the form as a query string. 
    * @return The form as a query string.
    */
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
    * Returns the formatted query corresponding to the current list of parameters.
    * @return The formatted query.
    */
   public Representation getWebForm() 
   {
      return Factory.getInstance().createRepresentation(getQueryString(), MediaType.APPLICATION_WWW_FORM);
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

}
