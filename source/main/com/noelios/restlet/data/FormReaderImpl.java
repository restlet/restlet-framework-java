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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.data.EmptyValue;
import org.restlet.data.FormReader;
import org.restlet.data.Parameter;

/**
 * Default form reader implementation.
 */
public class FormReaderImpl extends BufferedInputStream implements FormReader
{
   /**
    * Constructor.
    * @param contentStream The web form content stream.
    */
   public FormReaderImpl(InputStream contentStream)
   {
      super(contentStream);
   }

   /**
    * Reads the parameters with the given name. If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    */
   @SuppressWarnings("unchecked")
   public Object readParameter(String name) throws IOException
   {
      Parameter param = readNextParameter();
      Object result = null;

      while(param != null)
      {
         if(param.getName().equals(name))
         {
            if(result != null)
            {
               List<Object> values = null;

               if(result instanceof List)
               {
                  // Multiple values already found for this parameter
                  values = (List)result;
               }
               else
               {
                  // Second value found for this parameter
                  // Create a list of values
                  values = new ArrayList<Object>();
                  values.add(result);
                  result = values;
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
                  result = new EmptyValue();
               }
               else
               {
                  result = param.getValue();
               }
            }
         }

         param = readNextParameter();
      }

      close();
      return result;
   }

   /**
    * Reads the first parameter with the given name.
    * @param name The parameter name to match.
    * @return The parameter value.
    * @throws IOException
    */
   public Parameter readFirstParameter(String name) throws IOException
   {
      Parameter param = readNextParameter();
      Parameter result = null;

      while((param != null) && (result == null))
      {
         if(param.getName().equals(name))
         {
           result = param;
         }

         param = readNextParameter();
      }

      close();
      return result;
   }

   /**
    * Reads the parameters whose name is a key in the given map. If a matching parameter is found, its value
    * is put in the map. If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   @SuppressWarnings("unchecked")
   public void readParameters(Map<String, Object> parameters) throws IOException
   {
      Parameter param = readNextParameter();
      Object currentValue = null;

      while(param != null)
      {
         if(parameters.containsKey(param.getName()))
         {
            currentValue = parameters.get(param.getName());

            if(currentValue != null)
            {
               List<Object> values = null;

               if(currentValue instanceof List)
               {
                  // Multiple values already found for this parameter
                  values = (List)currentValue;
               }
               else
               {
                  // Second value found for this parameter
                  // Create a list of values
                  values = new ArrayList<Object>();
                  values.add(currentValue);
                  parameters.put(param.getName(), values);
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
                  parameters.put(param.getName(), new EmptyValue());
               }
               else
               {
                  parameters.put(param.getName(), param.getValue());
               }
            }
         }

         param = readNextParameter();
      }

      close();
   }

   /**
    * Reads the next parameter available or null.
    * @return The next parameter available or null.
    */
   public Parameter readNextParameter() throws IOException
   {
      Parameter result = null;

      try
      {
         boolean readingName = true;
         boolean readingValue = false;
         StringBuilder nameBuffer = new StringBuilder();
         StringBuilder valueBuffer = new StringBuilder();

         int nextChar = 0;
         while((result == null) && (nextChar != -1))
         {
            nextChar = read();

            if(readingName)
            {
               if(nextChar == '=')
               {
                  if(nameBuffer.length() > 0)
                  {
                     readingName = false;
                     readingValue = true;
                  }
                  else
                  {
                     throw new IOException("Empty parameter name detected. Please check your form data");
                  }
               }
               else if((nextChar == '&') || (nextChar == -1))
               {
                  if(nameBuffer.length() > 0)
                  {
                     result = createParameter(nameBuffer, null);
                  }
                  else if(nextChar == -1)
                  {
                     // Do nothing return null preference
                  }
                  else
                  {
                     throw new IOException("Empty parameter name detected. Please check your form data");
                  }
               }
               else
               {
                  nameBuffer.append((char)nextChar);
               }
            }
            else if(readingValue)
            {
               if((nextChar == '&') || (nextChar == -1))
               {
                  if(valueBuffer.length() > 0)
                  {
                     result = createParameter(nameBuffer, valueBuffer);
                  }
                  else
                  {
                     result = createParameter(nameBuffer, null);
                  }
               }
               else
               {
                  valueBuffer.append((char)nextChar);
               }
            }
         }
      }
      catch(UnsupportedEncodingException uee)
      {
         throw new IOException("Unsupported encoding. Please contact the administrator");
      }

      return result;
   }

   /**
    * Creates a parameter.
    * @param name The parameter name buffer.
    * @param value The parameter value buffer (can be null).
    * @return The created parameter.
    * @throws IOException
    */
   private Parameter createParameter(CharSequence name, CharSequence value) throws IOException
   {
      Parameter result = null;
      final String encoding = "UTF-8";

      try
      {
         if(name != null)
         {
            if(value != null)
            {
               result = new ParameterImpl(URLDecoder.decode(name.toString(), encoding), URLDecoder.decode(
                     value.toString(), encoding));
            }
            else
            {
               result = new ParameterImpl(URLDecoder.decode(name.toString(), encoding), null);
            }
         }
      }
      catch(UnsupportedEncodingException uee)
      {
         throw new IOException("Unsupported encoding exception. Please contact the administrator");
      }

      return result;
   }

}
