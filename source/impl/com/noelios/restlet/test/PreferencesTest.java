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

package com.noelios.restlet.test;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

import com.noelios.restlet.data.PreferenceReaderImpl;

/**
 * Unit tests for the Preference related classes. Based on JUnit framework.
 */
public class PreferencesTest extends TestCase
{
   /** Set-up the unit tests. */
   protected void setUp()
   {
   }

   /**
    * Tests the preferences parsing.
    * @throws RestletException
    */
   public void testParsing() throws IOException
   {
      String headerValue = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;LEVEL=2;q=0.4;ext1, */*;q=0.5";
      PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, headerValue);
      Preference pref = null;
      Parameter param = null;

      do
      {
         pref = pr.readPreference();

         if(pref != null)
         {
            System.out.println(pref.toString() + " = " + pref.getMetadata() + " , quality = "
                  + pref.getQuality());
            for(Iterator iter = pref.getParameters().iterator(); iter.hasNext();)
            {
               param = (Parameter)iter.next();
               System.out.println("Pref  param: " + param.getName() + " = " + param.getValue());
            }

            if(pref.getMetadata() instanceof MediaType)
            {
               MediaType mediaType = (MediaType)pref.getMetadata();
               for(Iterator iter = mediaType.getParameters().iterator(); iter.hasNext();)
               {
                  param = (Parameter)iter.next();
                  System.out.println("Media param: " + param.getName() + " = " + param.getValue());
               }
            }

            System.out.println("---------------------------------------------------------------------");
         }
      }
      while(pref != null);
   }

}
