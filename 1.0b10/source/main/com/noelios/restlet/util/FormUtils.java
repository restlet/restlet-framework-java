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

package com.noelios.restlet.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

/**
 * Representation of a Web form containing submitted parameters.
 */
public class FormUtils
{
   private static final String encoding = "UTF-8";

   /**
    * Parses a query into a given form.
    * @param form The target form.
    * @param query Query string.
    */
   public static void parseQuery(Form form, String query) throws IOException
   {
      new FormReader(query).addParameters(form);
   }
   
   /**
    * Parses a post into a given form.
    * @param form The target form.
    * @param post The posted form.
    */
   public static void parsePost(Form form, Representation post) throws IOException
   {
      new FormReader(post).addParameters(form);
   }

   /**
    * Reads the parameters whose name is a key in the given map.<br/>
    * If a matching parameter is found, its value is put in the map.<br/>
    * If multiple values are found, a list is created and set in the map.
    * @param query The query string.
    * @param parameters The parameters map controlling the reading.
    */
   public static void getParameters(String query, Map<String, Object> parameters) throws IOException
   {
      new FormReader(query).readParameters(parameters);
   }

   /**
    * Reads the parameters whose name is a key in the given map.<br/>
    * If a matching parameter is found, its value is put in the map.<br/>
    * If multiple values are found, a list is created and set in the map.
    * @param post The web form representation.
    * @param parameters The parameters map controlling the reading.
    */
   public static void getParameters(Representation post, Map<String, Object> parameters) throws IOException
   {
      new FormReader(post).readParameters(parameters);
   }
   
   /**
    * Reads the first parameter with the given name.
    * @param query The query string.
    * @param name The parameter name to match.
    * @return The parameter.
    * @throws IOException
    */
   public static Parameter getFirstParameter(String query, String name) throws IOException
   {
      return new FormReader(query).readFirstParameter(name);
   }
   
   /**
    * Reads the first parameter with the given name.
    * @param post The web form representation.
    * @param name The parameter name to match.
    * @return The parameter.
    * @throws IOException
    */
   public static Parameter getFirstParameter(Representation post, String name) throws IOException
   {
      return new FormReader(post).readFirstParameter(name);
   }
   
   /**
    * Reads the parameters with the given name.<br/>
    * If multiple values are found, a list is returned created.
    * @param query The query string.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    */
   public static Object getParameter(String query, String name) throws IOException
   {
      return new FormReader(query).readParameter(name);
   }
   
   /**
    * Reads the parameters with the given name.<br/>
    * If multiple values are found, a list is returned created.
    * @param form The web form representation.
    * @param name The parameter name to match.
    * @return The parameter value or list of values.
    */
   public static Object getParameter(Representation form, String name) throws IOException
   {
      return new FormReader(form).readParameter(name);
   }

   /**
    * Creates a parameter.
    * @param name The parameter name buffer.
    * @param value The parameter value buffer (can be null).
    * @return The created parameter.
    * @throws IOException
    */
   public static Parameter create(CharSequence name, CharSequence value) throws IOException
   {
   	Parameter result = null;

      try
      {
         if(name != null)
         {
            if(value != null)
            {
               result = new Parameter(URLDecoder.decode(name.toString(), encoding), URLDecoder.decode(
                     value.toString(), encoding));
            }
            else
            {
               result = new Parameter(URLDecoder.decode(name.toString(), encoding), null);
            }
         }
      }
      catch(UnsupportedEncodingException uee)
      {
         throw new IOException("Unsupported encoding exception. Please contact the administrator");
      }

      return result;
   }
   
   /**
    * Formats a list of parameters. 
    * @param parameters The list of parameters.
    * @return The encoded parameters string.
    * @throws IOException 
    */
   public static String format(List<Parameter> parameters) throws IOException
   {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < parameters.size(); i++)
      {
         if(i > 0) sb.append('&');
         format(parameters.get(i), sb);
      }
      return sb.toString();
   }
   
   /**
    * Formats a parameter.
    * @param parameter The parameter to format.
    * @return The encoded parameter string.
    * @throws IOException
    */
   public static String format(Parameter parameter) throws IOException
   {
      StringBuilder sb = new StringBuilder();
      format(parameter, sb);
      return sb.toString();
   }

   /**
    * Formats a parameter and append the result to the given buffer.
    * @param parameter The parameter to format.
    * @param buffer The buffer to append.
    * @throws IOException
    */
   public static void format(Parameter parameter, Appendable buffer) throws IOException
   {
      try
      {
         if(parameter != null)
         {
            if(parameter.getName() != null)
            {
               buffer.append(URLEncoder.encode(parameter.getName(), encoding));
               
               if(parameter.getValue() != null)
               {
                  buffer.append('=');
                  buffer.append(URLEncoder.encode(parameter.getValue(), encoding));
               }
            }
         }
      }
      catch(UnsupportedEncodingException uee)
      {
         throw new IOException("Unsupported encoding exception. Please contact the administrator");
      }
   }
   
}
