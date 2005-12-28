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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.FormReader;
import org.restlet.data.MediaTypes;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

/**
 * Default form implementation.
 */
public class FormImpl extends InputRepresentation implements Form
{
   /** Indicates if the reader for this form was already created. */
   protected boolean firstReaderCreation;

   /**
    * Constructor.
    * @param queryParameters The web form parameters as a string.
    */
   public FormImpl(String queryParameters)
   {
      super(new ByteArrayInputStream(queryParameters.getBytes()), MediaTypes.APPLICATION_WWW_FORM);
      this.firstReaderCreation = true;
   }

   /**
    * Construcotr.
    * @param requestContent The web form parameters as a representation.
    * @throws IOException
    */
   public FormImpl(Representation requestContent) throws IOException
   {
      super(requestContent.getStream(), MediaTypes.APPLICATION_WWW_FORM);
      this.firstReaderCreation = true;
   }

   /**
    * Reads the parameters with the given name. If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    */
   public Object getParameter(String name) throws IOException
   {
      return getFormReader().readParameter(name);
   }

   /**
    * Reads the first parameter with the given name.
    * @param name The parameter name to match.
    * @return The parameter value.
    * @throws IOException
    */
   public Parameter getFirstParameter(String name) throws IOException
   {
      return getFormReader().readFirstParameter(name);
   }

   /**
    * Reads the parameters whose name is a key in the given map. If a matching parameter is found, its value
    * is put in the map. If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void getParameters(Map<String, Object> parameters) throws IOException
   {
      getFormReader().readParameters(parameters);
   }

   /**
    * Returns a new form reader to read the list.
    * @return A new form reader to read the list.
    */
   public FormReader getFormReader() throws IOException
   {
      if(!firstReaderCreation && getStream().markSupported())
      {
         // Allow multiple uses of the form when possible
         getStream().reset();
      }

      firstReaderCreation = false;
      return new FormReaderImpl(getStream());
   }

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    * @see org.restlet.data.Parameter
    */
   public List<Parameter> getParameters() throws IOException
   {
      List<Parameter> result = new ArrayList<Parameter>();
      FormReader fis = getFormReader();
      Parameter param = fis.readNextParameter();

      while(param != null)
      {
         result.add(param);
         fis.readNextParameter();
      }

      fis.close();
      return result;
   }

}
