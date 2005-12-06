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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Default preference reader implementation.
 */
public class PreferenceReaderImpl extends HeaderReaderImpl
{
   public static final int TYPE_CHARACTER_SET = 1;

   public static final int TYPE_LANGUAGE = 2;

   public static final int TYPE_MEDIA_TYPE = 3;

   /** The type of metadata read. */
   protected int type;

   /**
    * Constructor.
    * @param type The type of metadata read.
    * @param headerValue The header value to read.
    */
   public PreferenceReaderImpl(int type, String headerValue)
   {
      this(type, new ByteArrayInputStream(headerValue.getBytes()));
   }

   /**
    * Constructor.
    * @param type The type of metadata read.
    * @param headerInputStream The header stream to read.
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

         while(currentPref != null)
         {
            result.add(currentPref);
            currentPref = readPreference();
         }
      }
      catch(IOException re)
      {
         re.printStackTrace();
      }

      return result;
   }

   /**
    * Read the next preference.
    * @return The next preference.
    * @throws RestletException
    */
   public Preference readPreference() throws IOException
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

      while((result == null) && (nextChar != -1))
      {
         nextChar = read();

         if(readingMetadata)
         {
            if((nextChar == ',') || (nextChar == -1))
            {
               if(metadataBuffer.length() > 0)
               {
                  // End of metadata section
                  // No parameters detected
                  result = createPreference(metadataBuffer, null);
                  paramNameBuffer = new StringBuilder();
               }
               else if(nextChar == -1)
               {
                  // Do nothing return null preference
               }
               else
               {
                  throw new IOException("Empty metadata name detected.");
               }
            }
            else if(nextChar == ';')
            {
               if(metadataBuffer.length() > 0)
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
                  throw new IOException("Empty metadata name detected.");
               }
            }
            else if(nextChar == ' ')
            {
               // Ignore white spaces
            }
            else if(isText(nextChar))
            {
               metadataBuffer.append((char)nextChar);
            }
            else
            {
               throw new IOException("Control characters are not allowed within a metadata name.");
            }
         }
         else if(readingParamName)
         {
            if(nextChar == '=')
            {
               if(paramNameBuffer.length() > 0)
               {
                  // End of parameter name section
                  readingParamName = false;
                  readingParamValue = true;
                  paramValueBuffer = new StringBuilder();
               }
               else
               {
                  throw new IOException("Empty parameter name detected.");
               }
            }
            else if((nextChar == ',') || (nextChar == -1))
            {
               if(paramNameBuffer.length() > 0)
               {
                  // End of parameters section
                  parameters.add(createParameter(paramNameBuffer, null));
                  result = createPreference(metadataBuffer, parameters);
               }
               else
               {
                  throw new IOException("Empty parameter name detected.");
               }
            }
            else if(nextChar == ';')
            {
               // End of parameter
               parameters.add(createParameter(paramNameBuffer, null));
               paramNameBuffer = new StringBuilder();
               readingParamName = true;
               readingParamValue = false;
            }
            else if((nextChar == ' ') && (paramNameBuffer.length() == 0))
            {
               // Ignore white spaces
            }
            else if(isTokenChar(nextChar))
            {
               paramNameBuffer.append((char)nextChar);
            }
            else
            {
               throw new IOException("Separator and control characters are not allowed within a token.");
            }
         }
         else if(readingParamValue)
         {
            if((nextChar == ',') || (nextChar == -1))
            {
               if(paramValueBuffer.length() > 0)
               {
                  // End of parameters section
                  parameters.add(createParameter(paramNameBuffer, paramValueBuffer));
                  result = createPreference(metadataBuffer, parameters);
               }
               else
               {
                  throw new IOException("Empty parameter value detected");
               }
            }
            else if(nextChar == ';')
            {
               // End of parameter
               parameters.add(createParameter(paramNameBuffer, paramValueBuffer));
               paramNameBuffer = new StringBuilder();
               readingParamName = true;
               readingParamValue = false;
            }
            else if((nextChar == '"') && (paramValueBuffer.length() == 0))
            {
               paramValueBuffer.append(readQuotedString());
            }
            else if(isTokenChar(nextChar))
            {
               paramValueBuffer.append((char)nextChar);
            }
            else
            {
               throw new IOException("Separator and control characters are not allowed within a token");
            }
         }
      }

      return result;
   }

   /**
    * Extract the media parameters. Only leaveas the quality parameter if found. Modifies the parameters list.
    * @param parameters All the preference parameters.
    * @return The media parameters.
    */
   private List<Parameter> extractMediaParams(List<Parameter> parameters)
   {
      List<Parameter> result = null;
      boolean qualityFound = false;
      Parameter param = null;

      if(parameters != null)
      {
         result = new ArrayList<Parameter>();

         for(Iterator iter = parameters.iterator(); !qualityFound && iter.hasNext();)
         {
            param = (Parameter)iter.next();

            if(param.getName().equals("q"))
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
    * Extract the quality value. If the value is not found, 1 is returned.
    * @param parameters The preference parameters.
    * @return The quality value.
    */
   private float extractQuality(List parameters)
   {
      Float result = null;

      if(parameters != null)
      {
         Parameter param = null;
         for(Iterator iter = parameters.iterator(); (result == null) && iter.hasNext();)
         {
            param = (Parameter)iter.next();
            if(param.getName().equals("q"))
            {
               result = Float.valueOf(param.getValue());

               // Remove the quality parameter as we will directly store it
               // in the Preference object
               iter.remove();
            }
         }
      }

      if(result == null)
      {
         result = new Float(1F);
      }

      if((result.floatValue() < 0F) || (result.floatValue() > 1F))
      {
         throw new IllegalArgumentException("Quality value must be between 0 and 1");
      }

      return result.floatValue();
   }

   /**
    * Creates a new preference.
    * @param metadata The metadata name.
    * @param parameters The parameters list.
    * @return The new preference.
    */
   private Preference createPreference(CharSequence metadata, List<Parameter> parameters)
   {
      Preference result = null;

      if(parameters == null)
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
    * @param metadata The metadata name.
    * @param parameters The parameters list.
    * @return The new metadata.
    */
   private Metadata createMetadata(CharSequence metadata, List<Parameter> parameters)
   {
      Metadata result = null;

      switch(type)
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
