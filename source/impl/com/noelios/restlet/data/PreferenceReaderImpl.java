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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.RestletException;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Default preference reader implementation.
 */
public class PreferenceReaderImpl extends HeaderReaderImpl
{
   public static final int TYPE_CHARACTER_SET = 1;
   public static final int TYPE_LANGUAGE      = 2;
   public static final int TYPE_MEDIA_TYPE    = 3;

   /** The type of metadata read. */
   protected int type;

   /**
    * Constructor.
    * @param type 			The type of metadata read.
    * @param headerValue	The header value to read.
    */
   public PreferenceReaderImpl(int type, String headerValue)
   {
      this(type, new ByteArrayInputStream(headerValue.getBytes()));
   }

   /**
    * Constructor.
    * @param type 					The type of metadata read.
    * @param headerInputStream	The header stream to read.
    */
   public PreferenceReaderImpl(int type, InputStream headerInputStream)
   {
      super(headerInputStream);
      this.type = type;
   }

   /**
    * Read all the preferences.
    * @return All the preferences read.
    */
   public List<Preference> readPreferences()
   {
      List<Preference> result = new ArrayList<Preference>();

      try
      {
         Preference currentPref = readPreference();

         while (currentPref != null)
         {
            result.add(currentPref);
            currentPref = readPreference();
         }
      }
      catch (RestletException re)
      {
         re.printStackTrace();
      }

      return result;
   }

   /**
    * Read the next preference.
    * @return The next preference.
    */
   public Preference readPreference() throws RestletException
   {
      Preference result = null;

      boolean readingMetadata = true;
      boolean readingParamName = false;
      boolean readingParamValue = false;

      StringBuilder metadataBuffer = new StringBuilder();
      StringBuilder paramNameBuffer = null;
      StringBuilder paramValueBuffer = null;

      List<Parameter> parameters = null;
      int nextChar = 0;

      try
      {
         while ((result == null) && (nextChar != -1))
         {
            nextChar = read();

            if (readingMetadata)
            {
               if ((nextChar == ',') || (nextChar == -1))
               {
                  if (metadataBuffer.length() > 0)
                  {
                     // End of metadata section
                     // No parameters detected
                     result = createPreference(metadataBuffer, null);
                     paramNameBuffer = new StringBuilder();
                  }
                  else if (nextChar == -1)
                  {
                     // Do nothing return null preference
                  }
                  else
                  {
                     throw new RestletException("Empty metadata name detected", "Please check your metadata names");
                  }
               }
               else if (nextChar == ';')
               {
                  if (metadataBuffer.length() > 0)
                  {
                     // End of metadata section
                     // Parameters detected
                     readingMetadata = false;
                     readingParamName = true;
                     paramNameBuffer = new StringBuilder();
                     parameters = new ArrayList<Parameter>();
                  }
                  else
                  {
                     throw new RestletException("Empty metadata name detected", "Please check your metadata names");
                  }
               }
               else if (nextChar == ' ')
               {
                  // Ignore white spaces
               }
               else if (isText(nextChar))
               {
                  metadataBuffer.append((char)nextChar);
               }
               else
               {
                  throw new RestletException("Control characters are not allowed within a metadata name",
                      "Please check your metadata names");
               }
            }
            else if (readingParamName)
            {
               if (nextChar == '=')
               {
                  if (paramNameBuffer.length() > 0)
                  {
                     // End of parameter name section
                     readingParamName = false;
                     readingParamValue = true;
                     paramValueBuffer = new StringBuilder();
                  }
                  else
                  {
                     throw new RestletException("Empty parameter name detected", "Please check your parameter names");
                  }
               }
               else if ((nextChar == ',') || (nextChar == -1))
               {
                  if (paramNameBuffer.length() > 0)
                  {
                     // End of parameters section
                     parameters.add(createParameter(paramNameBuffer, null));
                     result = createPreference(metadataBuffer, parameters);
                  }
                  else
                  {
                     throw new RestletException("Empty parameter name detected", "Please check your parameter names");
                  }
               }
               else if (nextChar == ';')
               {
                  // End of parameter
                  parameters.add(createParameter(paramNameBuffer, null));
                  paramNameBuffer = new StringBuilder();
                  readingParamName = true;
                  readingParamValue = false;
               }
               else if (isTokenChar(nextChar))
               {
                  paramNameBuffer.append((char)nextChar);
               }
               else
               {
                  throw new RestletException("Separator and control characters are not allowed within a token",
                      "Please check your parameter names");
               }
            }
            else if (readingParamValue)
            {
               if ((nextChar == ',') || (nextChar == -1))
               {
                  if (paramValueBuffer.length() > 0)
                  {
                     // End of parameters section
                     parameters.add(createParameter(paramNameBuffer, paramValueBuffer));
                     result = createPreference(metadataBuffer, parameters);
                  }
                  else
                  {
                     throw new RestletException("Empty parameter value detected", "Please check your parameter values");
                  }
               }
               else if (nextChar == ';')
               {
                  // End of parameter
                  parameters.add(createParameter(paramNameBuffer, paramValueBuffer));
                  paramNameBuffer = new StringBuilder();
                  readingParamName = true;
                  readingParamValue = false;
               }
               else if ((nextChar == '"') && (paramValueBuffer.length() == 0))
               {
                  paramValueBuffer.append(readQuotedString());
               }
               else if (isTokenChar(nextChar))
               {
                  paramValueBuffer.append((char)nextChar);
               }
               else
               {
                  throw new RestletException("Separator and control characters are not allowed within a token",
                      "Please check your parameter values");
               }
            }
         }
      }
      catch (IOException ioe)
      {
         throw new RestletException("Unexpected I/O exception", "Please contact the administrator");
      }

      return result;
   }

   /**
    * Extract the media parameters.
    * Only leaveas the quality parameter if found.
    * Modifies the parameters list.
    * @param 	All the preference parameters.
    * @return 	The media parameters.
    */
   private List<Parameter> extractMediaParams(List<Parameter> parameters)
   {
      List<Parameter> result = null;
      boolean qualityFound = false;
      Parameter param = null;

      if (parameters != null)
      {
         result = new ArrayList<Parameter>();

         for (Iterator iter = parameters.iterator(); !qualityFound && iter.hasNext(); )
         {
            param = (Parameter)iter.next();

            if (param.getName().equals("q"))
            {
               qualityFound = true;
            }
            else
            {
               iter.remove();
               result.add(param);
            }
         }
      }

      return result;
   }

   /**
    * Extract the quality value.
    * If the value is not found, 1 is returned.
    * @param 	The preference parameters.
    * @return 	The quality value.
    */
   private float extractQuality(List parameters)
   {
      Float result = null;

      if (parameters != null)
      {
         Parameter param = null;
         for (Iterator iter = parameters.iterator(); (result == null) && iter.hasNext(); )
         {
            param = (Parameter)iter.next();
            if (param.getName().equals("q"))
            {
               result = Float.valueOf(param.getValue());

               // Remove the quality parameter as we will directly store it
               // in the Preference object
               iter.remove();
            }
         }
      }

      if (result == null)
      {
         result = new Float(1F);
      }

      if ((result.floatValue() < 0F) || (result.floatValue() > 1F))
      {
         throw new IllegalArgumentException("Quality value must be between 0 and 1");
      }

      return result.floatValue();
   }

   /**
    * Creates a new preference.
    * @param metadata 	The metadata name.
    * @param parameters	The parameters list.
    * @return 				The new preference.
    */
   private Preference createPreference(CharSequence metadata, List<Parameter> parameters)
   {
      Preference result = null;

      if (parameters == null)
      {
         result = new PreferenceImpl(createMetadata(metadata, null));
      }
      else
      {
         List<Parameter> mediaParams = extractMediaParams(parameters);
         float quality = extractQuality(parameters);
         result = new PreferenceImpl(createMetadata(metadata, mediaParams), quality, parameters);
      }

      return result;
   }

   /**
    * Creates a new metadata.
    * @param metadata 	The metadata name.
    * @param parameters	The parameters list.
    * @return 				The new metadata.
    */
   private Metadata createMetadata(CharSequence metadata, List<Parameter> parameters)
   {
      Metadata result = null;

      switch (type)
      {
         case TYPE_CHARACTER_SET:
            result = new CharacterSetImpl(metadata.toString());
            break;

         case TYPE_LANGUAGE:
            result = new LanguageImpl(metadata.toString());
            break;

         case TYPE_MEDIA_TYPE:
            result = new MediaTypeImpl(metadata.toString(), parameters);
            break;
      }

      return result;
   }

}

