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

package com.noelios.restlet.test;

import com.noelios.restlet.data.PreferenceReaderImpl;

import java.util.Iterator;

import org.restlet.RestletException;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

import junit.framework.TestCase;

/**
 * Unit tests for the Preference related classes.
 * Based on JUnit framework.
 */
public class PreferencesTest extends TestCase
{
   /** Set-up the unit tests. */
   protected void setUp()
   {
   }

   /** Tests the preferences parsing. */
   public void testParsing() throws RestletException
   {
      String headerValue = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;LEVEL=2;q=0.4;ext1, */*;q=0.5";
      PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, headerValue);
      Preference pref = null;
      Parameter param = null;

      do
      {
         pref = pr.readPreference();

         if (pref != null)
         {
            System.out.println(pref.toString() + " = " + pref.getMetadata() + " , quality = " + pref.getQuality());
            for (Iterator iter = pref.getParameters().iterator(); iter.hasNext(); )
            {
               param = (Parameter)iter.next();
               System.out.println("Pref  param: " + param.getName() + " = " + param.getValue());
            }

            if (pref.getMetadata() instanceof MediaType)
            {
               MediaType mediaType = (MediaType)pref.getMetadata();
               for (Iterator iter = mediaType.getParameters().iterator(); iter.hasNext(); )
               {
                  param = (Parameter)iter.next();
                  System.out.println("Media param: " + param.getName() + " = " + param.getValue());
               }
            }

            System.out.println("---------------------------------------------------------------------");
         }
      }
      while (pref != null);
   }


}




