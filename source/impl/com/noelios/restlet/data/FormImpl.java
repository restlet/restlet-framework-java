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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.RestletException;
import org.restlet.data.Form;
import org.restlet.data.FormReader;
import org.restlet.data.MediaTypeEnum;
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
      super(new ByteArrayInputStream(queryParameters.getBytes()), MediaTypeEnum.APPLICATION_WWW_FORM);
      this.firstReaderCreation = true;
   }

   /**
    * Construcotr.
    * @param requestContent The web form parameters as a representation.
    */
   public FormImpl(Representation requestContent) throws RestletException
   {
      super(requestContent.getStream(), MediaTypeEnum.APPLICATION_WWW_FORM);
      this.firstReaderCreation = true;
   }

   /**
    * Reads the parameters with the given name.
    * If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return 		The parameter value or list of values.
    */
   public Object readParameter(String name) throws RestletException
   {
      return getFormReader().readParameter(name);
   }

   /**
    * Reads the parameters whose name is a key in the given map.
    * If a matching parameter is found, its value is put in the map.
    * If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void readParameters(Map<String, Object> parameters) throws RestletException
   {
      getFormReader().readParameters(parameters);
   }

   /**
    * Returns a new form reader to read the list.
    * @return A new form reader to read the list.
    */
   public FormReader getFormReader() throws RestletException
   {
      if (!firstReaderCreation && getStream().markSupported())
      {
         try
         {
            // Allow multiple uses of the form when possible
            getStream().reset();
         }
         catch (IOException ioe)
         {
            throw new RestletException("Couldn't reset the form stream", ioe);
         }
      }

      firstReaderCreation = false;
      return new FormReaderImpl(getStream());
   }

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    * @see org.restlet.data.Parameter
    */
   public List<Parameter> getParameters() throws RestletException
   {
      List<Parameter> result = new ArrayList<Parameter>();

      try
      {
         FormReader fis = getFormReader();
         Parameter param = fis.readParameter();
         while (param != null)
         {
            result.add(param);
            fis.readParameter();
         }
         fis.close();
      }
      catch (Exception e)
      {
         throw new RestletException("Error while reading the form parameters", e);
      }

      return result;
   }

}



