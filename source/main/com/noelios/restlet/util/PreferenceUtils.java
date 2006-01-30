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

package com.noelios.restlet.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Preference manipulation utilities.<br/>
 */
public class PreferenceUtils
{
   /**
    * Formats a list of preferences.
    * @param prefs The list of preferences.
    * @return The formatted list of preferences.
    * @throws IOException 
    */
   public static String format(List<? extends Preference> prefs) throws IOException
   {
      StringBuilder sb = new StringBuilder();
      
      Preference pref;
      for(int i = 0; i < prefs.size(); i++)
      {
         if(i > 0) sb.append(", ");
         pref = prefs.get(i);
         format(pref, sb);
      }      
      
      return sb.toString();
   }
   
   /**
    * Formats a preference.
    * @param pref The preference to format.
    * @param destination The appendable destination.
    * @throws IOException
    */
   public static void format(Preference pref, Appendable destination) throws IOException
   {
      destination.append(pref.getMetadata().getName());
      
      if(pref.getMetadata() instanceof MediaType)
      {
         MediaType mediaType = (MediaType)pref.getMetadata();
         
         if(mediaType.getParameters() != null)
         {
            Parameter param;
            for(Iterator<Parameter> iter = mediaType.getParameters().iterator(); iter.hasNext(); )
            {
               param = iter.next();
               
               if(param.getName() != null)
               {
                  destination.append(';').append(param.getName());
               
                  if((param.getValue() != null) && (param.getValue().length() > 0))
                  {
                     destination.append('=').append(param.getValue());
                  }
               }
            }
         }
      }
      
      if(pref.getQuality() < 1F)
      {
         destination.append(";q=");
         formatQuality(pref.getQuality(), destination);
      }
      
      if(pref.getParameters() != null)
      {
         Parameter param;
         for(Iterator<Parameter> iter = pref.getParameters().iterator(); iter.hasNext(); )
         {
            param = iter.next();
            
            if(param.getName() != null)
            {
               destination.append(';').append(param.getName());
            
               if((param.getValue() != null) && (param.getValue().length() > 0))
               {
                  destination.append('=').append(param.getValue());
               }
            }
         }
      }
   }
   
   /**
    * Formats a quality value.
    * @param quality The quality value as a float.
    * @param destination The appendable destination;
    * @throws IOException
    */
   public static void formatQuality(float quality, Appendable destination) throws IOException
   {
      if(!isQuality(quality)) 
      {
         throw new IllegalArgumentException("Invalid quality value detected. Value must be between 0 and 1.");
      }
      else
      {
         NumberFormat formatter = DecimalFormat.getNumberInstance(Locale.US);
         formatter.setMaximumFractionDigits(2);
         destination.append(formatter.format(quality));
      }
   }

   /**
    * Parses a quality value.
    * @param quality The quality value as a string.
    * @return The parsed quality value as a float.
    */
   public static float parseQuality(String quality)
   {
      try
      {
         float result = Float.valueOf(quality);
         
         if(isQuality(result))
         {
            return result;
         }
         else
         {
            throw new IllegalArgumentException("Invalid quality value detected. Value must be between 0 and 1.");
         }
      }
      catch(NumberFormatException nfe)
      {
         throw new IllegalArgumentException("Invalid quality value detected. Value must be between 0 and 1.");
      }
   }
   
   /**
    * Indicates if the quality value is valid.<br/>
    * Otherwise an IllegalArgumentException is thrown.
    * @param quality The quality value.
    * @return True if the quality value is valid.
    */
   public static boolean isQuality(float quality)
   {
      return (quality >= 0F) && (quality <= 1F);
   }
   
}
