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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of unit tests for the Restlet RI.
 * Based on JUnit framework.
 */
public class RestletTestSuite extends TestSuite
{
   /** Constructor. */
   public RestletTestSuite()
   {
      addTestSuite(PreferencesTest.class);
   }

   /** JUnit constructor. */
   public static Test suite()
   {
      return new RestletTestSuite();
   }

   /**
    * Main method to launch the TestRunner.
    * @param args Pass "swing" to launch the graphical runner, "text" otherwise.
    */
   public static void main(String[] args)
   {
      if (args.length > 0)
      {
         if (args[0].equals("swing"))
         {
            junit.swingui.TestRunner.run(RestletTestSuite.class);
         }
         else if (args[0].equals("text"))
         {
            junit.textui.TestRunner.run(suite());
         }
      }
   }

}




