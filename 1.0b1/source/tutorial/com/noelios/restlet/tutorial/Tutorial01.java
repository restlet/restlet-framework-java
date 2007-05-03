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

package com.noelios.restlet.tutorial;

/**
 * Registering the Restlet implementation
 */
public class Tutorial01
{
   public static void main(String[] args)
   {
      // Multiple options to register a Restlet API implementation
      
      // 1) By default, if no other implementation is registered,
      //    the Noelios Restlet Engine is automatically used.
            
      // 2) Manually registers an implementation by calling 
      //    the static register method on the implementation factory
      com.noelios.restlet.Engine.register();
      
      // 3) Set the factory class name in a system property
      //    a) Manually in your main method 
      System.setProperty("org.restlet.impl", "com.noelios.restlet.Engine");

      //    b) As a JVM argument
      // java -cp $CLASS_PATH -Dorg.restlet.impl=com.noelios.restlet.Engine $MAIN_CLASS $ARGS
   }
}
